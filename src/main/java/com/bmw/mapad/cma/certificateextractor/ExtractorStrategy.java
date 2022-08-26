package com.bmw.mapad.cma.certificateextractor;

import com.bmw.mapad.cma.entity.Cert;

import java.util.List;

/**
 * Interface for extract certificates from a specific file or directory. Any implementation of this abstraction should
 * retrieve a set of certificates from a specific file or directory.
 */
public interface ExtractorStrategy {
    /**
     * Export all digital certificates from a specific platform.
     * @return List of certificates.
     */
    List<Cert> exportAllCert();
}
