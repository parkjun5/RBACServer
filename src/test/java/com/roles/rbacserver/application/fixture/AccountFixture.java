package com.roles.rbacserver.application.fixture;

import com.roles.rbacserver.account.application.dto.AccountCreateRequest;
import com.roles.rbacserver.account.application.dto.AccountResponse;
import com.roles.rbacserver.account.application.dto.AccountRole;
import com.roles.rbacserver.account.domain.Account;

import java.util.Set;

public class AccountFixture {

    public static final AccountCreateRequest TEST_ACCOUNT_CREATE_REQUEST = new AccountCreateRequest("테스터", "q1w2e3!", Set.of(AccountRole.SYSTEM_ADMIN, AccountRole.NORMAL_USER));

    public static final Account TEST_ACCOUNT = Account.of(TEST_ACCOUNT_CREATE_REQUEST);

    public static final AccountResponse TEST_ACCOUNT_RESPONSE = AccountResponse.of(TEST_ACCOUNT);


}
