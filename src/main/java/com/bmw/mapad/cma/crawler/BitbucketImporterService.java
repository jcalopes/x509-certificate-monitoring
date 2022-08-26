package com.bmw.mapad.cma.crawler;

import com.bmw.mapad.cma.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a service which enable fetching all the files from a bitbucket repository.
 * Afterwards it´s filtered locally and retrieved the path files that matches with the set of extensions desired by the user.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BitbucketImporterService implements FilesImporter {
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String FOLDER_NAME = "projects";

    @Value("${cma.crawler.bitbucket.user}")
    String user;

    @Value("${cma.crawler.bitbucket.token}")
    String token;
    @Value("${cma.crawler.bitbucket.ignore-certs}")
    String[] certsToIgnore;

    final Utils utils;

    /**
     * Clone a specific git repository passed as argument.
     *
     * @param urlRepo The target repository to be cloned.
     */
    private String cloneRepository(String urlRepo) {
        String projectName = (urlRepo.lastIndexOf('/') != -1 && urlRepo.lastIndexOf('.') != -1) ?
                urlRepo.substring(urlRepo.lastIndexOf('/') + 1, urlRepo.lastIndexOf('.')) : "project";
        try {
            File file = new File(FOLDER_NAME + System.getProperty("file.separator") + projectName);
            if (file.mkdirs()) {
                Git.cloneRepository()
                        .setURI(urlRepo)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, token))
                        .setDirectory(Paths.get(FOLDER_NAME + System.getProperty("file.separator") + projectName).toFile())
                        .call()
                        .close();
                log.info("Repository cloned successfully: {}", projectName);
            }

        } catch (GitAPIException e) {
            log.warn("Error occurred while cloning the repo.");
        }
        return projectName;
    }

    /**
     * Return all the extensions file found after the scanning process within a folder.
     *
     * @return List of path files that matches the target extension file.
     * @throws IOException
     */
    private Map<String, String> scanForTargetFiles(List<String> extensions, String project) throws IOException {
        Map<String, String> result = new HashMap<>();

        try (Stream<Path> walk = Files.walk(Paths.get(FOLDER_NAME))) {
            walk
                    .filter(p -> !Files.isDirectory(p))
                    .map(Path::toString)
                    .filter(f -> utils.isEndWith(f, extensions))
                    .filter(f -> Arrays.stream(certsToIgnore)
                            .noneMatch(f::endsWith))
                    .forEach(p -> result.put(p, project));
        }
        return result;
    }

    /**
     * Clone a specific repository passed as parameter. If cloned successfully walk through the files.
     * tree within a specific folder and retrieve the path files by a given set of extensions files.
     *
     * @param repository Repository to be fetched.
     * @param extensions Extensions or name of the file to be sought within the repository aforementioned.
     * @return The collection of path file which matches with extensions aforementioned.
     */
    @Override
    public Map<String, String> collectFiles(String repository, List<String> extensions) {
        Map<String, String> filesFound = new HashMap<>();
        try {
            File file = new File(USER_DIR + System.getProperty("file.separator") + FOLDER_NAME);
            if (file.exists())
                utils.deleteFolder(FOLDER_NAME);
            if (file.mkdirs()) {
                String projectName = cloneRepository(repository);
                filesFound = scanForTargetFiles(extensions, projectName);
            }
        } catch (IOException e) {
            log.warn("An error occurred while scanning for files. " + e.getMessage());
        }
        return filesFound;
    }
}
