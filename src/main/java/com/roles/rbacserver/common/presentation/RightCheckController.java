package com.roles.rbacserver.common.presentation;

import com.roles.rbacserver.accountrole.application.annotation.NeedAccountRole;
import com.roles.rbacserver.common.application.dto.ApiCommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.roles.rbacserver.accountrole.AccountRole.*;

@RestController
@RequestMapping("/api/check")
public class RightCheckController {

    @PostMapping("/system-file")
    @NeedAccountRole(SYSTEM_ADMIN)
    public ResponseEntity<ApiCommonResponse> editSystemFiles() {
        return ApiCommonResponse.of(HttpStatus.OK, "SYSTEM_ADMIN 만 접근 할 수 있습니다.");
    }

    @GetMapping("/access/network")
    @NeedAccountRole({SYSTEM_ADMIN, NORMAL_USER, STUDENT})
    public ResponseEntity<ApiCommonResponse> accessNetwork() {
        return ApiCommonResponse.of(HttpStatus.OK, "SYSTEM_ADMIN, NORMAL_USER, STUDENT 만 접근 할 수 있습니다.");
    }

    @PostMapping("/user-file")
    @NeedAccountRole({SYSTEM_ADMIN, NORMAL_USER, LIMITED})
    public ResponseEntity<ApiCommonResponse> editUserFiles() {
        return ApiCommonResponse.of(HttpStatus.OK, "SYSTEM_ADMIN, NORMAL_USER, LIMITED 만 접근 할 수 있습니다.");
    }

    @GetMapping("/foo-bar-file")
    @NeedAccountRole({SYSTEM_ADMIN, STUDENT})
    public ResponseEntity<ApiCommonResponse> readFooBarFiles() {
        return ApiCommonResponse.of(HttpStatus.OK, "SYSTEM_ADMIN, STUDENT 만 접근 할 수 있습니다.");
    }

}
