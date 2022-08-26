package com.bmw.mapad.cma.entity;

import lombok.Data;

import java.util.*;


/**
 * This class represents a structure of the key store file uploaded.
 * This key store file has a set of cryptographic keys and certificates.
 */
@Data
public class KeystoreFile {
    private final String pathFile;
    private String nameFile;
    private List<Cert> storedCertificates = new ArrayList<>();
}
