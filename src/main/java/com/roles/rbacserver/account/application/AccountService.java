package com.roles.rbacserver.account.application;

import com.roles.rbacserver.account.application.dto.AccountCreateRequest;
import com.roles.rbacserver.account.application.dto.AccountResponse;
import com.roles.rbacserver.account.application.dto.AccountRoleUpdateRequest;
import com.roles.rbacserver.accountrole.AccountRole;
import com.roles.rbacserver.common.config.CustomPasswordEncoder;
import com.roles.rbacserver.account.domain.Account;
import com.roles.rbacserver.account.domain.repository.AccountRepository;
import com.roles.rbacserver.account.exception.AccountCreateException;
import com.roles.rbacserver.account.exception.PasswordEncryptException;
import com.roles.rbacserver.login.application.dto.LoginRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class AccountService {

    public static final String ACCOUNT_NOT_FOUND_MESSAGE = "아이디가 존재하지 않습니다.";
    private final AccountRepository accountRepository;
    private final CustomPasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, CustomPasswordEncoder customPasswordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = customPasswordEncoder;
    }

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        Account account = Account.of(request);
        try {
            account.changePassword(passwordEncoder.encrypt(request.password()));
        } catch (PasswordEncryptException e) {
            throw new AccountCreateException("유저 패스워드 암호화중 에러가 발생하였습니다.", e);
        }

        return AccountResponse.of(accountRepository.save(account));
    }

    public AccountResponse findById(Long id) {
        Account account = findByIdNotNull(id);
        return AccountResponse.of(account);
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = findByIdNotNull(id);
        account.deleteAccount();
    }

    public List<AccountResponse> findAllAccount() {
        return accountRepository.findAll().stream()
                .map(AccountResponse::of)
                .toList();
    }

    public boolean isCorrectNameAndPassword(LoginRequest request) throws PasswordEncryptException {
        Account account = accountRepository.findByName(request.name())
                .orElseThrow(() -> new NoSuchElementException(ACCOUNT_NOT_FOUND_MESSAGE));
        return passwordEncoder.matches(request.password(), account.getPassword());
    }

    public Set<AccountRole> findAccountRolesByName(String name) {
        return accountRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException(ACCOUNT_NOT_FOUND_MESSAGE))
                .getAccountRole();
    }

    @Transactional
    public void updateAccountRole(Long accountId, AccountRoleUpdateRequest request) {
        Account account = findByIdNotNull(accountId);
        account.changeAccountRole(request.accountRoleSet());
    }

    private Account findByIdNotNull(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(ACCOUNT_NOT_FOUND_MESSAGE));
    }

}
