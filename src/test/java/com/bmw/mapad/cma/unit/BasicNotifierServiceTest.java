package com.bmw.mapad.cma.unit;

import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.entity.CertX509;
import com.bmw.mapad.cma.notifier.basicNotifier.BasicNotifierService;
import com.bmw.mapad.cma.utils.Utils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Test class to check the behaviour of basic notifier in different scenarios.
 */
@ExtendWith(MockitoExtension.class)
public class BasicNotifierServiceTest {
    @InjectMocks
    BasicNotifierService notifier;
    @Mock
    Utils utils;
    static List<Cert> listCerts;

    /**
     * Set up the environment to run the following set of tests. Build a certificate that works 10 days before today and
     * expire 10 days after today. Will be tested scenarios which there is a need to notify on the edge of expiry date to assure
     * correct behavior.
     */
    @BeforeAll
    public static void setup() {
        LocalDate dateBefore = LocalDate.now().plusDays(10);
        LocalDate dateAfter = LocalDate.now().minusDays(10);
        Cert cert1 = CertX509.builder()
                .serialNumber(BigInteger.ONE)
                .alias("mocked cert")
                .startAfter( Date.from(dateAfter.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .finishBefore( Date.from(dateBefore.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .source("source unavailable")
                .build();

        listCerts = List.of(cert1);
    }

    /**
     * With one certificate expiring in 10 days I want to ensure that defining a timeframe of 20 days for trigger a notification
     * one certificate is returned.
     */
    @Test
    public void shouldReturnOneCert_When_OneCertExpire() {
        LocalDate targetDate10 = LocalDate.now().plusDays(20);
        when(utils.setLimitDateForNotification(20)).thenReturn(Date.from(targetDate10.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        List<Cert> listCert = notifier.notifyCertsByDate(listCerts, 20);
        Assertions.assertEquals(1, listCert.size());
    }


    /**
     * With one certificate expiring in 10 days I want to ensure that defining a timeframe of 5 days for trigger a notification
     * no one certificate is returned.
     */
    @Test
    public void shouldNotReturn_When_NoneCertExpire() {
        LocalDate targetDate10 = LocalDate.now().plusDays(5);
        when(utils.setLimitDateForNotification(5)).thenReturn(Date.from(targetDate10.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        List<Cert> listCert = notifier.notifyCertsByDate(listCerts, 5);
        Assertions.assertEquals(0, listCert.size());
    }

    /**
     * With one certificate expiring in 10 days I want to ensure that defining a timeframe of 9 days for trigger a notification
     * no one certificate is returned.
     */
    @Test
    public void shouldNotReturnCert_When_NoneCertExpire() {
        LocalDate targetDate10 = LocalDate.now().plusDays(9);
        when(utils.setLimitDateForNotification(9)).thenReturn(Date.from(targetDate10.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        List<Cert> listCert = notifier.notifyCertsByDate(listCerts, 9);
        Assertions.assertEquals(0, listCert.size());
    }

    /**
     * With one certificate expiring in 10 days I want to ensure that defining a timeframe of 10 days for trigger a notification
     * one certificate is eligible and returned as well.
     */
    @Test
    public void shouldReturnCert_When_ExpireWithin10Days() {
        LocalDate targetDate10 = LocalDate.now().plusDays(10);
        when(utils.setLimitDateForNotification(10)).thenReturn(Date.from(targetDate10.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        List<Cert> listCert = notifier.notifyCertsByDate(listCerts, 10);
        Assertions.assertEquals(1, listCert.size());
    }

    /**
     * With one certificate expiring in 10 days I want to ensure that defining a timeframe of 11 days for trigger a notification
     * one certificate is eligible and returned as well.
     */
    @Test
    public void shouldOneReturnCert_When_ExpireWithin10Days() {
        LocalDate targetDate10 = LocalDate.now().plusDays(11);
        when(utils.setLimitDateForNotification(11)).thenReturn(Date.from(targetDate10.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        List<Cert> listCert = notifier.notifyCertsByDate(listCerts, 11);
        Assertions.assertEquals(1, listCert.size());
    }
}
