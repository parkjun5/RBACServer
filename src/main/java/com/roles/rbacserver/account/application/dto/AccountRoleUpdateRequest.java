package com.roles.rbacserver.account.application.dto;

import com.roles.rbacserver.accountrole.AccountRole;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record AccountRoleUpdateRequest(
        @NotNull Set<AccountRole> accountRoleSet
) {
}
