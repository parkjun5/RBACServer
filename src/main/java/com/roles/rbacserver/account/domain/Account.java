package com.roles.rbacserver.account.domain;

import com.roles.rbacserver.account.application.dto.AccountCreateRequest;
import com.roles.rbacserver.account.application.dto.AccountRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

import static jakarta.persistence.GenerationType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "account_id")
    private Long id;

    private String name;

    private String password;

    private boolean isDeleted;

    @Enumerated(EnumType.STRING)
    private Set<AccountRole> accountRole;


    public static Account of(AccountCreateRequest request) {
        Account account = new Account();
        account.name = request.name();
        account.accountRole = request.accountRoleSet();
        account.isDeleted = false;
        return account;
    }

    public void changePassword(String encryptPassword) {
        this.password = encryptPassword;
    }

    public void deleteAccount() {
        this.isDeleted = true;
    }
}
