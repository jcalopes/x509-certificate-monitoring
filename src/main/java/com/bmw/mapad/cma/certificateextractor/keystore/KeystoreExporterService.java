package com.bmw.mapad.cma.certificateextractor.keystore;

import com.bmw.mapad.cma.certificateextractor.ExtractorStrategy;
import com.bmw.mapad.cma.crawler.FilesImporter;
import com.bmw.mapad.cma.entity.CertX509;
import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.credentialsmanagement.CredentialsManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bmw.mapad.cma.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Service to open a keystore file (ex: .kdbx) extracting certificates stored.
 * Relies on the CredentialsManager implementation to load the credentials and then find out the matching password to each keystore file.
 */
@Slf4j
@RequiredArgsConstructor
@Service("jks")
public class KeystoreExporterService implements ExtractorStrategy {
    @Value("${cma.crawler.bitbucket.repositories}")
    String[] repositoriesList;
    final FilesImporter bitbucketHandler;
    private final CredentialsManager credentialsDB;
    final Utils utils;

    /**
     * Returns a KeyStore file which contains a set of digital certificates.
     * This keystore file are protected by a master key password to ensure security to the storage process.
     *
     * @param pathFile Path of target keystore file.
     */
    public Optional<KeyStore> loadFile(String pathFile, String project) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        List<String> passwordList = credentialsDB.findPassword(pathFile, project);
        Optional<KeyStore> keystore = Optional.of(KeyStore.getInstance(KeyStore.getDefaultType()));
        for (String password : passwordList) {
            File keyFile = new File(pathFile);
            try (InputStream inputStream = new FileInputStream(keyFile)) {
                keystore.get().load(inputStream, password.toCharArray());
                log.info("{} Unlocked successfully.",pathFile);
                return keystore;
            } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
                //Not logging intentionally to avoid throw a bunch of logs resulting of the process to find out the correct password
                //If not found the matching password incident will be reported later
            }
        }

        File keyFile = new File(pathFile);
        try (InputStream inputStream = new FileInputStream(keyFile)) {
            keystore.get().load(inputStream, null);
            log.info("{} Unlocked successfully.",pathFile);
            return keystore;
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns all certificates stored in this keystore file.
     *
     * @param pathFile Required password to unlock this key store file.
     * @return The set of certificates stored in this key store file.
     */
    public List<Cert> exportCertfromFile(String pathFile, String project) {
        List<Cert> storedCertificates = new ArrayList<>();
        try {
            Optional<KeyStore> keystore = loadFile(pathFile, project);
            if (keystore.isPresent()) {
                Enumeration<String> enumeration = keystore.get().aliases();
                while (enumeration.hasMoreElements()) {
                    String alias = enumeration.nextElement();
                    Certificate certificate = keystore.get().getCertificate(alias);
                    if (certificate!= null && "X.509".equals(certificate.getType())) {
                        X509Certificate currentCert = (X509Certificate) certificate;
                        Cert loadedCertificate = CertX509.builder()
                                .serialNumber(currentCert.getSerialNumber())
                                .alias(alias)
                                .startAfter(currentCert.getNotBefore())
                                .finishBefore(currentCert.getNotAfter())
                                .project(project)
                                .source(pathFile)
                                .issueID("NO_ISSUE")
                                .build();
                        storedCertificates.add(loadedCertificate);
                    }else{
                        log.info("Certificate {} is null: {}",pathFile, enumeration);
                    }
                }
            }else{
                log.error("Any password found for keystore: {}", pathFile);
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }

        return storedCertificates;
    }

    /**
     * Export all certificates from keystore files.
     * @return List of certificates.
     */
    @Override
    public List<Cert> exportAllCert(){
        List<Cert> listCert = new ArrayList<>();
        try {
            for(String repo: repositoriesList){
                Map<String, String> pathFiles = bitbucketHandler.collectFiles(repo, List.of("jks"));
                pathFiles.forEach((keystoreFile, project) -> {
                    listCert.addAll(exportCertfromFile(keystoreFile, project));
                });
                utils.deleteFolder("projects");
            }
        } catch (IOException e) {log.error(e.getMessage());}
        return listCert;
    }
}