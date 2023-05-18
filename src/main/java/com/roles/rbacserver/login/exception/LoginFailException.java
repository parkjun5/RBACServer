package com.roles.rbacserver.login.exception;

import com.roles.rbacserver.common.exception.BusinessException;

public class LoginFailException extends BusinessException{
    public LoginFailException(String message) {
        super(message);
    }

    public LoginFailException(String message, Throwable cause) {
        super(message, cause);
    }
}
