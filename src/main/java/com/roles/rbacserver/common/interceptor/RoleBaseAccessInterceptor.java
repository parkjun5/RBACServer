package com.roles.rbacserver.common.interceptor;

import com.roles.rbacserver.account.application.AccountService;
import com.roles.rbacserver.accountrole.AccountRole;
import com.roles.rbacserver.accountrole.application.annotation.NeedAccountRole;
import com.roles.rbacserver.login.application.JwtTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.Collections;
import java.util.Set;

//TODO: 인터셉터 개선
// 통합 A,B,C

/**
 * 이 인터셉터는 역할 기반 접근 제어(Role-Based Access Control, RBAC)를 위해 사용됩니다.
 */
@Component
public class RoleBaseAccessInterceptor implements HandlerInterceptor {

    private final AccountService accountService;
    private final JwtTokenService jwtTokenService;

    /**
     * RoleBaseAccessInterceptor의 인스턴스를 생성합니다.
     *
     * @param accountService     계정 서비스
     * @param jwtTokenService    JWT 토큰 서비스
     */
    public RoleBaseAccessInterceptor(AccountService accountService, JwtTokenService jwtTokenService) {
        this.accountService = accountService;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 들어오는 요청을 사전 처리합니다.
     * 토큰이 올바른지와 유효한지 확인합니다.
     * 그런 다음 토큰의 역할이 요청된 리소스에 접근할 수 있는지 확인합니다.
     *
     * @param request  요청
     * @param response 응답
     * @param handler  핸들러
     * @return 토큰이 올바르고 유효하며 역할이 리소스에 접근할 수 있을 경우 true, 그렇지 않을 경우 false
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        boolean hasAnnotation = hasNeedAccountRoleAnnotation(handler);
        if (!hasAnnotation) {
            return true;
        }

        String token = request.getHeader("Authorization");
        boolean isTokenCorrectFormat = StringUtils.hasText(token) && token.startsWith("Bearer ");
        if (!isTokenCorrectFormat) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        token = token.substring(7);
        boolean isValidToken = jwtTokenService.validateToken(token);
        if (!isValidToken) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String name = jwtTokenService.getNameFromToken(token);
        Set<AccountRole> accountRoles = accountService.findAccountRolesByName(name);
        Set<AccountRole> requiredRole = getAccountRole(handler);
        boolean containRole = requiredRole.stream().anyMatch(accountRoles::contains);
        if (!containRole) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        return containRole;
    }

    private boolean hasNeedAccountRoleAnnotation(Object handler) {
        if (handler instanceof ResourceHttpRequestHandler) {
            return true;
        }

        if (handler instanceof HandlerMethod handlerMethod) {
            return handlerMethod.hasMethodAnnotation(NeedAccountRole.class);
        }

        return false;
    }

    private Set<AccountRole> getAccountRole(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            NeedAccountRole needAccountRole = handlerMethod.getMethodAnnotation(NeedAccountRole.class);
            return needAccountRole != null ? Set.of(needAccountRole.value()) : Collections.emptySet();
        }

        return Collections.emptySet();
    }
}
