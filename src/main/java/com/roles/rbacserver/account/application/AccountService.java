package com.roles.rbacserver.account.application;

import com.roles.rbacserver.account.application.dto.AccountCreateRequest;
import com.roles.rbacserver.account.application.dto.AccountResponse;
import com.roles.rbacserver.common.config.CustomPasswordEncoder;
import com.roles.rbacserver.account.domain.Account;
import com.roles.rbacserver.account.domain.repository.AccountRepository;
import com.roles.rbacserver.account.exception.AccountCreateException;
import com.roles.rbacserver.account.exception.PasswordEncryptException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomPasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, CustomPasswordEncoder customPasswordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = customPasswordEncoder;
    }

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


    public AccountResponse findByName(String name) {
        Account account = accountRepository.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("아이디가 존재하지 않습니다."));
        return AccountResponse.of(account);
    }

    public void deleteAccount(Long id) {
        Account account = findByIdNotNull(id);
        account.deleteAccount();
    }

    private Account findByIdNotNull(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("아이디가 존재하지 않습니다."));
    }

    public List<AccountResponse> findAllAccount() {
        return accountRepository.findAll().stream()
                .map(AccountResponse::of)
                .toList();
    }
}
