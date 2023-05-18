package com.roles.rbacserver.login.application.dto;

import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(
        @NotEmpty String name,
        @NotEmpty String password
) {
}
