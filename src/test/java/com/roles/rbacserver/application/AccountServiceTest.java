package com.roles.rbacserver.application;

import com.roles.rbacserver.account.application.AccountService;
import com.roles.rbacserver.common.config.CustomPasswordEncoder;
import com.roles.rbacserver.account.domain.Account;
import com.roles.rbacserver.account.domain.repository.AccountRepository;
import com.roles.rbacserver.account.exception.PasswordEncryptException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.roles.rbacserver.application.fixture.AccountFixture.TEST_ACCOUNT;
import static com.roles.rbacserver.application.fixture.AccountFixture.TEST_ACCOUNT_CREATE_REQUEST;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CustomPasswordEncoder passwordEncoder;

    @Test
    @DisplayName("계정 생성 테스트")
    void createAccountTest() throws PasswordEncryptException {
        //given
        given(passwordEncoder.encrypt(anyString())).willReturn("변환된 비밀번호");
        given(accountRepository.save(any(Account.class))).willReturn(TEST_ACCOUNT);

        //when
        accountService.createAccount(TEST_ACCOUNT_CREATE_REQUEST);

        //then
        then(passwordEncoder).should(times(1)).encrypt(anyString());
        then(accountRepository).should(times(1)).save(any(Account.class));
        then(accountRepository).should(times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("계정 ID로 조회 테스트")
    void findByIdTest() {
        //given
        given(accountRepository.findById(anyLong())).willReturn(Optional.of(TEST_ACCOUNT));

        //when
        accountService.findById(anyLong());

        //then
        then(accountRepository).should(times(1)).findById(anyLong());
    }

    @Test
    @DisplayName("계정 삭제 테스트 실제로 삭제되지 않고 플래그 변경")
    void deleteAccountTest() {
        //given
        given(accountRepository.findById(anyLong())).willReturn(Optional.of(TEST_ACCOUNT));

        //when
        accountService.deleteAccount(anyLong());

        //then
        then(accountRepository).should(times(1)).findById(anyLong());
        assertThat(TEST_ACCOUNT.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("모든 계정 출력")
    void findAllTest() {
        //given
        given(accountRepository.findAll()).willReturn(List.of(TEST_ACCOUNT));

        //when
        accountService.findAllAccount();

        //then
        then(accountRepository).should(times(1)).findAll();
    }
}