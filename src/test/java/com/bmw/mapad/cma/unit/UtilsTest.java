package com.bmw.mapad.cma.unit;

import com.bmw.mapad.cma.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UtilsTest {
    static Utils utils;

    @BeforeAll
    static void setup(){
        utils = new Utils();
    }

    /**
     * Use case scenario with valid email.
     */
    @Test
    public void shouldReturnTrue_WhenCorrectEmail(){
        //Given valid email
        String validEmail = "username@domain.com";

        //When check structure -> Then
        Assertions.assertTrue(utils.isValidEmail(validEmail));
    }

    /**
     * Use case scenario with a couple of invalid emails.
     */
    @Test
    public void shouldReturnFalse_WhenIncorrectEmail(){
        //Given a set of invalid emails
        String invalidEmail = ".user.name@domain.com";
        String invalidEmail1 = "user-name@domain.com.";
        String invalidEmail2 = "username@.com";

        //When check their structure -> Then
        Assertions.assertFalse(utils.isValidEmail(invalidEmail));
        Assertions.assertFalse(utils.isValidEmail(invalidEmail1));
        Assertions.assertFalse(utils.isValidEmail(invalidEmail2));
    }
}
