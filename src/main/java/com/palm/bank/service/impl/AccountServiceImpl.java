package com.palm.bank.service.impl;

import com.palm.bank.entity.AccountEntity;
import com.palm.bank.repository.AccountRepository;
import com.palm.bank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("accountService")
public class AccountServiceImpl implements AccountService {

    @Autowired
    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountEntity create(String name, String password) {
        String address = "address"; // todo, generate EOA address by using web3j
        AccountEntity accountEntity = AccountEntity.builder().name(name).password(password).address(address).build();
        return accountRepository.save(accountEntity);
    }

    @Override
    public AccountEntity findByName(String name) {
        return accountRepository.findByName(name);
    }
}
