package com.palm.bank.service.impl;

import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.AccountTokenEntity;
import com.palm.bank.repository.AccountRepository;
import com.palm.bank.repository.AccountTokenRepository;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.LoginService;
import com.palm.bank.util.TokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service("loginService")
public class LoginServiceImpl implements LoginService {

    private final int EXPIRE_TIME = 1000 * 60 * 60 * 3; // 3 days

    @Autowired
    private final AccountService accountService;

    @Autowired
    private final AccountTokenRepository accountTokenRepository;

    public LoginServiceImpl(AccountService accountService, AccountTokenRepository accountTokenRepository) {
        this.accountService = accountService;
        this.accountTokenRepository = accountTokenRepository;
    }

    @Override
    public AccountTokenEntity login(String name, String password) {
        AccountEntity accountEntity = accountService.findByName(name);
        if (accountEntity == null) {
            log.error("not found account by name: {}", name);
            return null;
        }

        if (accountEntity.getPassword().compareTo(password) == 0) {
            String token = TokenGenerator.generate();
            Date expireTime = new Date(new Date().getTime() + EXPIRE_TIME);
            AccountTokenEntity accountTokenEntity = AccountTokenEntity.builder().accountId(accountEntity.getId()).token(token).expireTime(expireTime).build();
            accountTokenRepository.save(accountTokenEntity);
            return accountTokenEntity;
        }
        log.error("not match password for account: {}", name);

        return null;
    }

    @Override
    public AccountTokenEntity isValid(String token) {
        AccountTokenEntity accountTokenEntity = accountTokenRepository.findByToken(token);
        if (accountTokenEntity == null || accountTokenEntity.getExpireTime() == null) {
            return null;
        }
        if (accountTokenEntity.getExpireTime().before(new Date())) {
            return null;
        }
        return accountTokenEntity;
    }
}
