package com.bmw.mapad.cma.unit;

import com.bmw.mapad.cma.certificateextractor.keystore.KeystoreExporterService;
import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.credentialsmanagement.KeepassCredentialsService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KeystoreServiceTest {
    @Mock
    KeepassCredentialsService keypassTool;
    @InjectMocks
    KeystoreExporterService keystoreExtractor;
    static String correctPassword = "pass123";

    /**
     * Test load a keystore file with the correct password.
     */
    @Test
    public void tc1_ecp1_loadKeystore() {
        when(keypassTool.findPassword("sampleCertificates/keystore.with.60.days.jks","")).thenReturn(List.of("wrongPass1", correctPassword));

        //Given
        String pathForKeystoreFile = "sampleCertificates/keystore.with.60.days.jks";

        //When -> Then
        Assertions.assertDoesNotThrow(() -> keystoreExtractor.loadFile(pathForKeystoreFile,""));
    }

    /**
     * Test load keystore with an incorrect password.
     */
    @Test
    public void tc2_ecp2_loadKeystore() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        when(keypassTool.findPassword("sampleCertificates/keystore.with.60.days.jks","")).thenReturn(List.of("wrongPass1", "wrongPass2"));

        //Given
        String pathForKeystoreFile = "sampleCertificates/keystore.with.60.days.jks";

        //When -> Then
        Assertions.assertDoesNotThrow(() -> keystoreExtractor.loadFile(pathForKeystoreFile,""));
        Assertions.assertFalse(keystoreExtractor.loadFile(pathForKeystoreFile,"").isPresent());
    }
}