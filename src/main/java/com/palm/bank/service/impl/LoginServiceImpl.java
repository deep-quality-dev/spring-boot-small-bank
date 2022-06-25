package com.palm.bank.service.impl;

import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.AccountTokenEntity;
import com.palm.bank.repository.AccountRepository;
import com.palm.bank.repository.AccountTokenRepository;
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
    private final AccountRepository accountRepository;

    @Autowired
    private final AccountTokenRepository accountTokenRepository;

    public LoginServiceImpl(AccountRepository accountRepository, AccountTokenRepository accountTokenRepository) {
        this.accountRepository = accountRepository;
        this.accountTokenRepository = accountTokenRepository;
    }

    @Override
    public AccountTokenEntity login(String name, String password) {
        AccountEntity accountEntity = accountRepository.findByName(name);
        if (accountEntity == null) {
            log.info("Not found account by name: {}", name);
            return null;
        }

        if (accountEntity.getPassword().compareTo(password) == 0) {
            log.info("Not match password for account: {}", name);
            String token = TokenGenerator.generate();
            Date expireTime = new Date(new Date().getTime() + EXPIRE_TIME);
            AccountTokenEntity accountTokenEntity = AccountTokenEntity.builder().accountId(accountEntity.getId()).token(token).expireTime(expireTime).build();
            accountTokenRepository.save(accountTokenEntity);
            return accountTokenEntity;
        }

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
