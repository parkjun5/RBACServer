package com.roles.rbacserver.rolebaseaccess.application.annotation;


import com.roles.rbacserver.account.application.dto.AccountRole;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Target({METHOD})
@Retention(RUNTIME)
@Documented
public @interface  NeedAccountRole {
    AccountRole[] value();
}
