package com.palm.bank.service.impl;

import com.palm.bank.config.BankConfig;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.repository.AccountRepository;
import com.palm.bank.service.AccountService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("accountService")
public class AccountServiceImpl implements AccountService {

    private final BankConfig bankConfig;

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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // This function is overrided from `UserDetailsService` interface, will be called when authorizing
        AccountEntity accountEntity = this.findByName(username);
        if (accountEntity == null) {
            throw new UsernameNotFoundException(username);
        }

        return new User(accountEntity.getName(), accountEntity.getEncodedPassword(),
                true, true, true, true, new ArrayList<>());
    }
}
