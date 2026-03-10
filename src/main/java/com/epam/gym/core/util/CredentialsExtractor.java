package com.epam.gym.core.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Base64;

public class CredentialsExtractor {

    private CredentialsExtractor() {
    }

    public static String[] extractCredentials(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Basic ")) {
            throw new SecurityException("Missing or invalid Authorization header");
        }
        String decoded = new String(Base64.getDecoder().decode(header.substring(6)));
        int colonIndex = decoded.indexOf(':');
        if (colonIndex < 0) {
            throw new SecurityException("Invalid Authorization header format");
        }
        return new String[]{decoded.substring(0, colonIndex), decoded.substring(colonIndex + 1)};
    }
}
