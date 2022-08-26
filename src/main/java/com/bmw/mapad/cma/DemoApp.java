package com.bmw.mapad.cma;

import com.bmw.mapad.cma.certificateextractor.ExtractorContext;
import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.notifier.NotifierContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.util.*;


@RequiredArgsConstructor
@Slf4j
@Service
@SpringBootApplication
public class DemoApp implements CommandLineRunner {
    final ExtractorContext extractorContext;
    final NotifierContext notifierContext;

    public static void main(String[] args) {
        SpringApplication.run(DemoApp.class);
    }

    @Override
    public void run(String... args) {
            //Extract
            List<Cert> extractedCert = extractorContext.exportCert();

            //Notify
            log.info("Certs expired or expire within 31 days. Total : {}", notifierContext.notify(extractedCert, 31));
    }
}

