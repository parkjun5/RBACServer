package com.roles.rbacserver.account.presentation;

import com.roles.rbacserver.account.application.AccountService;
import com.roles.rbacserver.account.application.dto.AccountCreateRequest;
import com.roles.rbacserver.account.application.dto.AccountRoleUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Object> createAccount(
            @RequestBody @Valid AccountCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    @GetMapping
    public ResponseEntity<Object> getAllAccounts() {
        return ResponseEntity.ok(accountService.findAllAccount());
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Object> getAccountById(
            @PathVariable final Long accountId
    ) {
        return ResponseEntity.ok(accountService.findById(accountId));
    }

    @PostMapping("/role/{accountId}")
    public ResponseEntity<Object> updateAccountRole(
            @PathVariable final Long accountId, @RequestBody @Valid AccountRoleUpdateRequest request
    ) {
        accountService.updateAccountRole(accountId, request);
        return ResponseEntity.ok("updated");
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<String> deleteAccount(
            @PathVariable final Long accountId
    ) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.ok("deleted");
    }

}
