package com.bmw.mapad.cma.certificateextractor.acm;

import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.certificatemanager.model.*;
import com.bmw.mapad.cma.certificateextractor.ExtractorStrategy;
import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.entity.CertX509;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to manage certificates interacting with Amazon Certificate Manager using AWS SDK.
 */
@Service("acm")
@RequiredArgsConstructor
public class AcmExporterService implements ExtractorStrategy {
    final AWSCertificateManager acmClient;

    /**
     * Export all certificates from amazon certificate manager in some specific region and account.
     *
     * @return List of certs successfully exported.
     */
    @Override
    public List<Cert> exportAllCert() {
        List<Cert> listCert = new ArrayList<>();
        ListCertificatesRequest request = new ListCertificatesRequest();
        request.setCertificateStatuses(Arrays
                .stream(CertificateStatus.values())
                .map(CertificateStatus::toString)
                .collect(Collectors.toList()));
        request.setMaxItems(100);

        ListCertificatesResult arnCertList = acmClient.listCertificates(request);

        arnCertList.getCertificateSummaryList().forEach((item) -> {
            DescribeCertificateRequest reqCert = new DescribeCertificateRequest();
            reqCert.setCertificateArn(item.getCertificateArn());
            CertificateDetail certInfo = acmClient.describeCertificate(reqCert).getCertificate();
            listCert.add(CertX509.builder()
                    .serialNumber(BigInteger.ONE)
                    .alias(certInfo.getCertificateArn())
                    .startAfter(certInfo.getNotBefore())
                    .finishBefore(certInfo.getNotAfter())
                    .project(certInfo.getDomainName())
                    .source("Certificate Manager")
                    .issueID("NO_ISSUE")
                    .build());
        });
        return listCert;
    }
}
