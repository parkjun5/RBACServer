package com.roles.rbacserver.login.presentation;

import com.roles.rbacserver.login.application.LoginService;
import com.roles.rbacserver.login.application.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/api/login")
    public ResponseEntity<String> login(
            @RequestBody @Valid LoginRequest request
    ) {
       return ResponseEntity.ok("Authorization: Bearer " + loginService.login(request));
    }
}
