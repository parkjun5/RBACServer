package com.roles.rbacserver.rolebaseaccess.interceptor;

import com.roles.rbacserver.account.application.AccountService;
import com.roles.rbacserver.account.application.dto.AccountRole;
import com.roles.rbacserver.rolebaseaccess.application.AccountRoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashSet;
import java.util.Set;

@Component
public class RoleBaseAccessInterceptor implements HandlerInterceptor {

    private final AccountService accountService;
    private final AccountRoleService accountRoleService;

    public RoleBaseAccessInterceptor(AccountService accountService, AccountRoleService accountRoleService) {
        this.accountService = accountService;
        this.accountRoleService = accountRoleService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        Set<AccountRole> requiredRole = accountRoleService.getRoleInfoByURI(request.getMethod().toUpperCase(), requestURI);
        if (requiredRole == null || requiredRole.contains(AccountRole.ALL)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Set<AccountRole> accountRoles = accountService.findByName(authorization).accountRoleSet();
        HashSet<AccountRole> copiedSet = new HashSet<>(requiredRole);
        copiedSet.retainAll(accountRoles);
        if (!copiedSet.isEmpty()) {
            return true;
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
    }
}
