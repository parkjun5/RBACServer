package com.roles.rbacserver.account.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record AccountCreateRequest(
        @NotEmpty String name,
        @NotEmpty String password,
        @NotNull Set<AccountRole> accountRoleSet
) {
}
