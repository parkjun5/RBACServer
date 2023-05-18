package com.roles.rbacserver.account.application.dto;


import com.roles.rbacserver.account.domain.Account;
import com.roles.rbacserver.accountrole.AccountRole;

import java.util.Set;

public record AccountResponse(
        String name,
        Set<AccountRole> accountRoleSet
) {
    public static AccountResponse of(Account account) {
        return new AccountResponse(account.getName(), account.getAccountRole());
    }
}
