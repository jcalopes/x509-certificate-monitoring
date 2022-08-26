package com.bmw.mapad.cma.utils;

import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.entity.CertX509;
import com.bmw.mapad.cma.notifier.jiraService.dto.TicketsInfo;
import com.bmw.mapad.cma.utils.exceptions.ThrowingPredicate;
import com.bmw.mapad.cma.utils.httpClient.JiraApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Service
public class Utils {
    /**
     * Returns the target date.
     *
     * @param days Number of the days.
     * @return Limit date for trigger a notification to check a certificate validity.
     */
    public Date setLimitDateForNotification(int days) {
        Calendar targetDate = Calendar.getInstance();
        targetDate.setTime(new Date());
        targetDate.add(Calendar.DAY_OF_MONTH, days);
        return targetDate.getTime();
    }

    /**
     * Check if a specific file matches with any of extensions specified in fileExtensions list.
     *
     * @param file           Target file to be evaluated.
     * @param fileExtensions Set of extensions file allowed.
     * @return Boolean
     */
    public boolean isEndWith(String file, List<String> fileExtensions) {
        for (String fileExtension : fileExtensions) {
            if (file.endsWith(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract and retrieve the extension of the file passed as argument.
     *
     * @param filename Target file.
     * @return The extension of the file.
     */
    public Optional<String> getExtensionFile(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    /**
     * Delete all files and directories within a given directory passed as argument.
     *
     * @param strPath
     * @throws IOException
     */
    public void deleteFolder(String strPath) throws IOException {
        Path pathFile = Paths.get(strPath);
        try(Stream<Path> walk = Files.walk(pathFile)){
                walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }


    /**
     * Consider use this when working with lambda function and there is a need to handle any kind of exception.
     * Removes the boilerplate code of try/catch across the application by building a single generic function to deal with
     * checked exceptions. This prevents raise some exception, but generates a log in runtime to facilitate debugging work.
     * @param predicate Lambda function.
     * @return Predicate.
     * @param <T> The type of the input to the predicate
     */
    public <T> Predicate<T> handleException(ThrowingPredicate<? super T,?> predicate) {
        Objects.requireNonNull(predicate);
        return t -> {
            try {
                return predicate.apply(t);
            } catch (Exception e) {
                log.error("An error occurred: {}", e.toString());
                return false;
            }
        };
    }


    /**
     * Consider using this function to validate the email passed as argument
     * by using a regex expression from RFC5322 standard.
     * @param emailAddress Email address to be validated.
     * @return true if the regex expression matches with email passed as argument otherwise return false.
     */
    public boolean isValidEmail(String emailAddress) {
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }

    /**
     * Export a set of certificates to a common separate values files(.csv).
     * @param listCerts List of certificate to be written in file.
     * @param filename File name of the resulting file.
     * @param headers Headers to be set in csv file.
     * @throws IOException if some problem happens in writing csv file.
     */
    public void exportToCsvFormat(List<Cert> listCerts, String filename, Object... headers) throws IOException, ParseException {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try(CSVPrinter printer = new CSVPrinter(new FileWriter(filename + ".csv"), CSVFormat.EXCEL)){
            printer.printRecord(headers);
            for (Cert cert:listCerts) {
                printer.printRecord(cert.getAlias(), cert.getProject(),cert.getSource(),
                        simpleDateFormat.format(cert.getStartAfter()),simpleDateFormat.format(cert.getFinishBefore()),cert.getIssueID());
            }
        }
    }

    public List<Cert> importCsvFile(String filename) throws ParseException {
        Reader in = null;
        List<Cert> certsLastRun = new ArrayList<>();
        try {
            in = new FileReader(filename);

            Iterable<CSVRecord> records = CSVFormat.Builder.create()
                    .setHeader()
                    .setAllowMissingColumnNames(false)
                    .build()
                    .parse(in);
            for (CSVRecord record : records) {
                Cert cert = CertX509.builder()
                        .serialNumber(BigInteger.ONE)
                        .alias(record.get("Alias"))
                        .startAfter(new SimpleDateFormat("yyyy-MM-dd").parse(record.get("Start_date")))
                        .finishBefore(new SimpleDateFormat("yyyy-MM-dd").parse(record.get("Expiration_date")))
                        .project(record.get("Project"))
                        .source(record.get("Source"))
                        .issueID(record.get("IssueID"))
                        .build();
                certsLastRun.add(cert);
            }
        } catch (IOException e) {
            log.info("Jira report not found.");
        }
        return certsLastRun;
    }

    /**
     * Check past issues created by this application whose are still unresolved in order to prevent
     * create duplicated issues.
     * @param jiraApi Http client to make the request about tickets information.
     * @param projectId Project ID to looking for within this project.
     * @param label Project label to filter the tickets.
     * @return Tickets found with aforementioned characteristics.
     */
    public List<Cert> checkUnresolvedIssues(JiraApi jiraApi,String projectId, String label,List<Cert> listCerts) throws IOException {
        List<Cert> certsReported;
        Response<TicketsInfo> ticketsFound = jiraApi.getIssues(String.format("project=%s&labels=%s&resolution=unresolved", projectId, label)).execute();
        if ((ticketsFound.code() == 200 && ticketsFound.body() != null)) {
            certsReported = ticketsFound.body().mapToCert();
            log.info("{} unresolved tickets were found associated to this project.", ticketsFound.body().total);
        } else {
            certsReported = new ArrayList<>();
            log.error("Code {}:Msg:{}.An error occurred fetching historic issues.This could lead to duplicate issues.",
                    ticketsFound.code(), ticketsFound.errorBody());
        }

        for (Cert oldCert : certsReported) {
            for (Cert newCert : listCerts) {
                if (oldCert.getAlias().equals(newCert.getAlias()) || newCert.getAlias().endsWith(oldCert.getAlias())) {
                    newCert.setIssueID(oldCert.getIssueID());
                }
            }
        }
        return listCerts;
    }
}
