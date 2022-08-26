package com.bmw.mapad.cma.credentialsmanagement;

import java.util.List;

/**
 * Interface for credentials manager. Provide an abstraction for manage a credentials repository and
 * provide a strategy to guess and retrieve the candidate's password to open a specific security artifact.
 */
public interface CredentialsManager {
    /**
     * Check if exists some entry in database file by a given certificate passed as argument.
     * @param targetEntry Certificate to be searched in loaded entries from database file.
     * @return The password if exist.
     */
    List<String> findPassword(String targetEntry,String project);
}
