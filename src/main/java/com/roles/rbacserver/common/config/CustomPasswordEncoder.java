package com.roles.rbacserver.common.config;


import com.roles.rbacserver.account.exception.PasswordEncryptException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component

public class CustomPasswordEncoder {

    public String encrypt(String password) throws PasswordEncryptException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new PasswordEncryptException("Error encoding password", e);
        }
    }

    public boolean matches(String rawPassword, String encodedPassword) throws PasswordEncryptException {
        String encryptedPassword = encrypt(rawPassword);
        return encryptedPassword.equals(encodedPassword);
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}