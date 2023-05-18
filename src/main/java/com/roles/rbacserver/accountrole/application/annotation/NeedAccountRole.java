package com.roles.rbacserver.accountrole.application.annotation;


import com.roles.rbacserver.accountrole.AccountRole;

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
