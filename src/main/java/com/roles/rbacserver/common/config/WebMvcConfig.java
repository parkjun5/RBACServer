package com.roles.rbacserver.common.config;

import com.roles.rbacserver.rolebaseaccess.application.AccountRoleService;
import com.roles.rbacserver.rolebaseaccess.interceptor.RoleBaseAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AccountRoleService accountRoleFinder;
    private final RoleBaseAccessInterceptor roleBaseAccessInterceptor;

    public WebMvcConfig(AccountRoleService accountRoleFinder, RoleBaseAccessInterceptor roleBaseAccessInterceptor) {
        this.accountRoleFinder = accountRoleFinder;
        this.roleBaseAccessInterceptor = roleBaseAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        accountRoleFinder.initURIAndAccountRole();
        List<String> uriAnnotatedNeedAccountRole = accountRoleFinder.findURIAnnotatedNeedAccountRole().stream().toList();
        registry.addInterceptor(roleBaseAccessInterceptor)
                .addPathPatterns(uriAnnotatedNeedAccountRole);
    }
}
