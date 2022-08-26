package com.bmw.mapad.cma.unit;

import com.bmw.mapad.cma.entity.Cert;
import com.bmw.mapad.cma.entity.CertX509;
import com.bmw.mapad.cma.notifier.emailService.EmailNotifierService;
import com.bmw.mapad.cma.utils.Utils;
import com.bmw.mapad.cma.utils.exceptions.InvalidEmailFormatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class EmailNotifierTest {
    @Mock
    Utils utils;

    @InjectMocks
    EmailNotifierService emailNotifier;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(emailNotifier, "templateMessage", "It's a template message");
        ReflectionTestUtils.setField(emailNotifier, "senderEntity", "testValidEmail@domain.com");
    }

    /**
     * Test build an email structure supplying correct emails.
     */
    @Test
    public void shouldNotThrow_WhenCorrectEmail() {
        when(utils.isValidEmail("testValidEmail@domain.com")).thenReturn(true);

        //Given
        List<Cert> certList = new ArrayList<>();
        certList.add(
                CertX509.builder()
                        .serialNumber(BigInteger.ONE)
                        .alias("alias")
                        .startAfter(new Date())
                        .finishBefore(new Date())
                        .source("source/mockedCert.")
                        .build());

        //When build email structure then should not throw any exception.
       Assertions.assertDoesNotThrow(()->emailNotifier.buildMailStructure(certList,"testValidEmail@domain.com"));
    }

    /**
     * Test build an email structure supplying incorrect emails.
     */
    @Test
    public void shouldThrow_WhenIncorrectEmail(){
        when(utils.isValidEmail("testInvalidEmail@com")).thenReturn(false);

        //Given
        List<Cert> certList = new ArrayList<>();
        certList.add(
                CertX509.builder()
                        .serialNumber(BigInteger.ONE)
                        .alias("alias")
                        .startAfter(new Date())
                        .finishBefore(new Date())
                        .source("source/mockedCert.")
                        .build());

        //When build email structure then should not throw any exception.
        Assertions.assertThrows(InvalidEmailFormatException.class,
                ()-> emailNotifier.buildMailStructure(certList,"testInvalidEmail@com"));
    }
}
