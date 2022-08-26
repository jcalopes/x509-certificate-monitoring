package com.bmw.mapad.cma.credentialsmanagement;


import com.bmw.mapad.cma.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class stores and exports all the credentials required to unlock keystore files.
 * The file must have .kdbx extension which is associated a one specific keepass database archive.
 */
@Service("Keepass")
@Slf4j
@ConditionalOnProperty(prefix = "cma.crawler.bitbucket.credentials", name = "type", havingValue = "keepass")
@RequiredArgsConstructor
public class KeepassCredentialsService implements CredentialsManager {
    public static final String FOLDER_NAME = "projects";
    @Value("${cma.crawler.bitbucket.credentials.file}")
    private String pathFile;
    @Value("${cma.crawler.bitbucket.credentials.pass}")
    private String password;
    final Utils utils;
    Boolean loaded = false;

    /**
     * Load all passwords from a keepass database file. Keepass database file provide a secure way to store a set of passwords.
     * KeePass database file is encrypted using a master key.
     *
     * @param file Path for the database file.
     */
    public void loadCredentials(String file) {
        if (!loaded) {
            loaded = true;
            Optional<String> dbPathFile = Optional.empty();
            try (Stream<Path> walk = Files.walk(Paths.get(FOLDER_NAME))) {
                dbPathFile = walk.filter(p -> !Files.isDirectory(p))
                        .map(Path::toString)
                        .filter(f -> utils.isEndWith(f, List.of(file)))
                        .findFirst();
            } catch (IOException e) {
                log.error("An error occurred loading credentials {}", e.getMessage());
            }

            if(dbPathFile.isPresent()){
                ProcessBuilder builder = new ProcessBuilder();
                builder.command("sh", "-c", String.format("mv %s .",dbPathFile.get()));
                try {
                    Process process = builder.start();
                    log.info("Result code: {}", process.waitFor());
                } catch (IOException | InterruptedException e) {
                    log.error("{}",e.getMessage());
                }
                log.info("Keepass file found: {}",dbPathFile.get());
            }else{
                log.error("Keepass file not found!");
            }
        }
    }

    /**
     * This method allow performing operations over keepass-cli dependency. This dependency provides several commands
     * to handle with keepass files.
     * @param command Desired command to call some operation from keepass-cli dependency.
     * @param entry Keystore keyword(s) to looking for into Keepass file,
     * @return Process builder to execute.
     */
    ProcessBuilder makeProcessBuilder(String command, String entry) {
        ProcessBuilder builder = new ProcessBuilder();
        Map<String, String> instructionMap = Map.ofEntries(
                new AbstractMap.SimpleEntry<>("show", "show -s"),
                new AbstractMap.SimpleEntry<>("locate", "locate"));
        String instruction = String.format("keepassxc-cli %s %s '%s'", instructionMap.get(command), pathFile, entry);
        builder.command("sh", "-c", instruction);
        return builder;
    }

    /**
     * This method try to find out some entries within a keepass file that contains the keyword(s) passed as argument.
     * @param command Command used by keepass-cli to return the entries found.
     * @param entry Keyword(s) to looking for by keepass-cli dependency.
     * @return All the entries from a keepass file that contains the desired keywords.
     * @throws IOException
     * @throws InterruptedException
     */
    List<String> getAllCredentialsByKeyword(String command, String entry) throws IOException, InterruptedException {
        List<String> results = new ArrayList<>();
        Process process = makeProcessBuilder(command, entry).start();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        PrintWriter passwordPrompt = new PrintWriter(process.getOutputStream());
        passwordPrompt.println(password);
        passwordPrompt.flush();
        String line;
        while ((line = br.readLine()) != null) {
            if (command.equals("locate")) {
                results.add(line);
            } else if (line.contains("Password: ")) {
                results.add(line.replace("Password: ", ""));
            }
        }
        process.waitFor();
        return results;
    }

    /**
     * Given the name of certificate is performed a set of operations to map the credential from a keepass file with
     * the target keystore to open it and read its certificates.
     *
     * @param targetEntry Certificate to be searched in loaded entries from keepass database file.
     * @return List of candidate passwords.
     */
    @Override
    public List<String> findPassword(String targetEntry, String project) {
        loadCredentials(pathFile);
        List<String> possibilities = new ArrayList<>();
        List<String> keywordsFromCertAndProject = Arrays.stream(targetEntry.split("[.-]"))
                .map(String::toLowerCase).collect(Collectors.toList());
        keywordsFromCertAndProject.addAll(Arrays.stream(targetEntry.split("-"))
                .map(String::toLowerCase).collect(Collectors.toList()));

        List<String> entries = new ArrayList<>();
        try {
            for (String keyword : keywordsFromCertAndProject) {
                List<String> results = getAllCredentialsByKeyword("locate", keyword);
                for (String entry : results) {
                    if (!entries.contains(entry)) {
                        entries.add(entry);
                    }
                }
            }
            for (String entry : entries) {
                List<String> pass = getAllCredentialsByKeyword("show", entry);
                possibilities.addAll(pass);
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return possibilities;
    }
}