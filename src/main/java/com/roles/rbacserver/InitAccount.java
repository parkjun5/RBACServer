package com.roles.rbacserver;

import com.roles.rbacserver.account.application.AccountService;
import com.roles.rbacserver.account.application.dto.AccountCreateRequest;
import com.roles.rbacserver.accountrole.AccountRole;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.roles.rbacserver.accountrole.AccountRole.*;

@Component
public class InitAccount {

    private final AccountService accountService;

    public InitAccount(AccountService accountService) {
        this.accountService = accountService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initAccount() {
        List<AccountCreateRequest> requests = List.of(
                createRequestFixture("A", EnumSet.of(SYSTEM_ADMIN, NORMAL_USER, LIMITED, STUDENT)),
                createRequestFixture("B", EnumSet.of(NORMAL_USER)),
                createRequestFixture("C", EnumSet.of(NORMAL_USER)),
                createRequestFixture("D", EnumSet.of(NORMAL_USER, LIMITED)),
                createRequestFixture("E", EnumSet.of(STUDENT)),
                createRequestFixture("F", EnumSet.of(STUDENT)),
                createRequestFixture("G", EnumSet.of(STUDENT)),
                createRequestFixture("H", EnumSet.of(LIMITED)),
                createRequestFixture("I", EnumSet.of(LIMITED)),
                createRequestFixture("J", EnumSet.of(LIMITED))
        );

        requests.forEach(accountService::createAccount);
    }

    private AccountCreateRequest createRequestFixture(String name, Set<AccountRole> accountRoleSet) {
        return new AccountCreateRequest(name, "123", accountRoleSet);
    }
}
