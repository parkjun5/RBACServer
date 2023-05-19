package com.roles.rbacserver.common.config;

import com.roles.rbacserver.accountrole.application.AccountRoleService;
import com.roles.rbacserver.common.interceptor.RoleBaseAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AccountRoleService accountRoleService;
    private final RoleBaseAccessInterceptor roleBaseAccessInterceptor;

    public WebMvcConfig(AccountRoleService accountRoleService, RoleBaseAccessInterceptor roleBaseAccessInterceptor) {
        this.accountRoleService = accountRoleService;
        this.roleBaseAccessInterceptor = roleBaseAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleBaseAccessInterceptor)
                .addPathPatterns("/api/**")
                .addPathPatterns(accountRoleService.findAnnotatedUris());
    }
}
