package com.roles.rbacserver.login.exception;

public class IllegalTokenException extends RuntimeException {
    public IllegalTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
