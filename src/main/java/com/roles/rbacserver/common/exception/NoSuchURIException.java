package com.roles.rbacserver.common.exception;

public class NoSuchURIException extends BusinessException {
    public NoSuchURIException(String message) {
        super(message);
    }

    public NoSuchURIException(String message, Throwable cause) {
        super(message, cause);
    }
}
