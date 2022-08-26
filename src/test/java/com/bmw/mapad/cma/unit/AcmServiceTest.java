package com.bmw.mapad.cma.unit;

import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.certificatemanager.model.*;
import com.bmw.mapad.cma.certificateextractor.acm.AcmExporterService;
import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.entity.CertX509;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AcmServiceTest {
    @InjectMocks
    AcmExporterService acmExporter;
    @Mock
    static AWSCertificateManager awsClient;
    static ListCertificatesResult certReturned = new ListCertificatesResult();
    static DescribeCertificateResult result1;

    @BeforeAll
    static void setUp(){
        result1 = new DescribeCertificateResult();
        CertificateDetail certInfo1 = new CertificateDetail()
                .withCertificateArn("arn-test-1")
                .withNotBefore(new Date())
                .withNotAfter(new Date())
                .withDomainName("test.ctw");
        result1.setCertificate(certInfo1);
    }

    /**
     * Given one certificate in ACM when our Acm service fetch all the certificates
     * then should return exactly one with the same arn.
     */
    @Test
    public void should_CertBeEqual_When_AcmHasOneCert() {
        //Given response from AWS SDK with exactly 1 certificate
        certReturned.setCertificateSummaryList(new ArrayList<>() {
            {
                add(new CertificateSummary());
            }
        });
        when(awsClient.listCertificates(any())).thenReturn(certReturned);
        when(awsClient.describeCertificate(any())).thenReturn(result1);

        Cert expectedCert1 = CertX509.builder()
                .serialNumber(BigInteger.ONE)
                .alias("arn-test-1")
                .startAfter(new Date())
                .finishBefore(new Date())
                .project("test.ctw")
                .source("certificate manager")
                .build();

        //When fetch from ACM
        List<Cert> certsFound = acmExporter.exportAllCert();

        //Then
        Assertions.assertEquals(1, certsFound.size());
        Assertions.assertTrue(certsFound.stream()
                .anyMatch(cert -> cert.equals(expectedCert1)));

    }

    /**
     * Given one certificate in ACM when our Acm service fetch all the certificates
     * then should return exactly one with the same arn.
     */
    @Test
    public void should_ReturnZeroCert_When_NotExistCert() {
        //Given response from AWS SDK with exactly 0 certificates
        certReturned.setCertificateSummaryList(new ArrayList<>() {});
        when(awsClient.listCertificates(any())).thenReturn(certReturned);

        //When fetch from ACM
        List<Cert> certsFound = acmExporter.exportAllCert();

        //Then
        Assertions.assertEquals(0, certsFound.size());
    }
}