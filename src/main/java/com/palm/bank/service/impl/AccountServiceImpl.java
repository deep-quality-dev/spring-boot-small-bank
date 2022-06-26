package com.palm.bank.service.impl;

import com.palm.bank.config.BankConfig;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.repository.AccountRepository;
import com.palm.bank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("accountService")
public class AccountServiceImpl implements AccountService {

    @Autowired
    private final BankConfig bankConfig;

    @Autowired
    private final AccountRepository accountRepository;

    public AccountServiceImpl(BankConfig bankConfig, AccountRepository accountRepository) {
        this.bankConfig = bankConfig;
        this.accountRepository = accountRepository;
    }

    @Override
    public boolean save(AccountEntity accountEntity) {
        accountRepository.save(accountEntity);
        return true;
    }

    @Override
    public List<AccountEntity> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public AccountEntity findById(Long accountId) {
        return accountRepository.findById(accountId).get();
    }

    @Override
    public AccountEntity findByName(String name) {
        return accountRepository.findByName(name);
    }

    @Override
    public AccountEntity findByAddress(String address) {
        return accountRepository.findByAddress(address);
    }
}
