package com.roles.rbacserver.login.presentation;

public class IllegalTokenException extends RuntimeException {
    public IllegalTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
