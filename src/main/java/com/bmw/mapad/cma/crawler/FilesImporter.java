package com.bmw.mapad.cma.crawler;

import java.util.List;
import java.util.Map;

/**
 * Interface for a file's importer. Provide an abstraction for implement a component that enable their users to
 * find, analyse, filter and retrieve a set of desired files from a specific repository. Repository might be a git management repository as bitbucket, gitlab, etc. or
 * another platform.
 *
 */
public interface FilesImporter {
    /**
     * Retrieve the path of files that matches with the couple of extensions provided by the user. These files are fetched from valid repositories
     * passed as argument.
     * @param repository Target repository to fetch the files that match with extensions passed as argument.
     * @param extensions Set of extensions to filter the files.
     * @return List of path files.
     */
    Map<String,String> collectFiles(String repository, List<String> extensions);
}
