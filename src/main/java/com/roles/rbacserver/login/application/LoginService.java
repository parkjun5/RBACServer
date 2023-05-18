package com.roles.rbacserver.login.application;

import com.roles.rbacserver.account.application.AccountService;
import com.roles.rbacserver.account.exception.PasswordEncryptException;
import com.roles.rbacserver.login.application.dto.LoginRequest;
import com.roles.rbacserver.login.exception.LoginFailException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private static final String LOGIN_FAIL_MESSAGE = "아이디 혹은 비밀번호가 잘못되었습니다.";
    private final AccountService accountService;
    private final JwtTokenService jwtTokenService;

    public LoginService(AccountService accountService, JwtTokenService jwtTokenService) {
        this.accountService = accountService;
        this.jwtTokenService = jwtTokenService;
    }

    public String login(@Valid LoginRequest request) {
        boolean isLoginSuccessful;

        try {
            isLoginSuccessful = accountService.isCorrectNameAndPassword(request);
        } catch (PasswordEncryptException e) {
            throw new LoginFailException(LOGIN_FAIL_MESSAGE, e);
        }

        if (!isLoginSuccessful) {
            throw new LoginFailException(LOGIN_FAIL_MESSAGE);
        }

        return jwtTokenService.generateToken(request.name());
    }


}
