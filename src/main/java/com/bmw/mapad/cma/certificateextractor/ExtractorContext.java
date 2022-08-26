package com.bmw.mapad.cma.certificateextractor;

import com.bmw.mapad.cma.entity.Cert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Context where are placed all the different strategies available to handle with different sources in order to extract
 * certificates.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ExtractorContext {
    final Map<String,ExtractorStrategy> exporterStrategies;

    @Value("${cma.extractor.strategies}")
    String[] strategies;

    /**
     * Export all cert from several ways and formats defined externally.
     * Depending on the platform this sources might not be necessary.
     *
     * @return The list of certs which has been extracted.
     */
    public List<Cert> exportCert() {
        List<Cert> extractedCert = new ArrayList<>();
        Arrays.asList(strategies).forEach((strategy) -> {
            extractedCert.addAll(exporterStrategies.get(strategy).exportAllCert());
        });

        List<Cert> extractedDistinctCert = new ArrayList<>();
        for (Cert cert:extractedCert) {
            if(!extractedDistinctCert.contains(cert)){
                extractedDistinctCert.add(cert);
            }
        }
        return extractedDistinctCert;
    }
}