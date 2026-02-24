package com.epam.gym.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGeneratorImpl();
    }

    @Test
    void generatePassword_ReturnNonNullPassword() {
        String password = passwordGenerator.generatePassword();
        assertNotNull(password);
    }

    @Test
    void generatePassword_ReturnPasswordWithCorrectLength() {
        String password = passwordGenerator.generatePassword();
        assertEquals(10, password.length(), "Password should be exactly 10 characters long");
    }

    @Test
    void generatePassword_ContainOnlyAlphanumericCharacters() {
        String password = passwordGenerator.generatePassword();
        assertTrue(password.matches("[A-Za-z0-9]+"),
                "Password should contain only alphanumeric characters");
    }
}
