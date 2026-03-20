package com.epam.gym.core.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CredentialsExtractorTest {

    private static String encode(String value) {
        return "Basic " + Base64.getEncoder().encodeToString(value.getBytes());
    }

    @Test
    void extractCredentials_validHeaderReturnsUsernameAndPassword() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", encode("alice:secret"));

        String[] credentials = CredentialsExtractor.extractCredentials(request);

        assertArrayEquals(new String[]{"alice", "secret"}, credentials);
    }

    @Test
    void extractCredentials_passwordWithColonReturnsCorrectSplit() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", encode("alice:pass:with:colons"));

        String[] credentials = CredentialsExtractor.extractCredentials(request);

        assertArrayEquals(new String[]{"alice", "pass:with:colons"}, credentials);
    }

    @Test
    void extractCredentials_missingHeaderThrowsSecurityException() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThrows(SecurityException.class, () -> CredentialsExtractor.extractCredentials(request));
    }

    @Test
    void extractCredentials_nonBasicSchemeThrowsSecurityException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer sometoken");

        assertThrows(SecurityException.class, () -> CredentialsExtractor.extractCredentials(request));
    }

    @Test
    void extractCredentials_noColonInDecodedThrowsSecurityException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic " +
                Base64.getEncoder().encodeToString("usernameonly".getBytes()));

        assertThrows(SecurityException.class, () -> CredentialsExtractor.extractCredentials(request));
    }
}
