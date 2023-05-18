package com.roles.rbacserver.common.presentation;

import com.roles.rbacserver.accountrole.application.annotation.NeedAccountRole;
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
    public ResponseEntity<Object> editSystemFiles() {
        return ResponseEntity.ok("only SYSTEM_ADMIN can access");
    }

    @GetMapping("/access/network")
    @NeedAccountRole({SYSTEM_ADMIN, NORMAL_USER, STUDENT})
    public ResponseEntity<Object> accessNetwork() {
        return ResponseEntity.ok("SYSTEM_ADMIN, NORMAL_USER, STUDENT can access");
    }

    @PostMapping("/user-file")
    @NeedAccountRole({SYSTEM_ADMIN, NORMAL_USER, LIMITED})
    public ResponseEntity<Object> editUserFiles() {
        return ResponseEntity.ok("SYSTEM_ADMIN, NORMAL_USER, LIMITED can access");
    }

    @GetMapping("/foo-bar-file")
    @NeedAccountRole({SYSTEM_ADMIN, STUDENT})
    public ResponseEntity<Object> readFooBarFiles() {
        return ResponseEntity.ok("SYSTEM_ADMIN, STUDENT can access");
    }

}
