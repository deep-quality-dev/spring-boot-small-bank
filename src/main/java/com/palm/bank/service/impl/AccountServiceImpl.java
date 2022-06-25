package com.palm.bank.service.impl;

import com.palm.bank.config.BankConfig;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.repository.AccountRepository;
import com.palm.bank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

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
    public AccountEntity saveOne(String name, String password) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException {
        String filename = WalletUtils.generateNewWalletFile(password, new File(bankConfig.getKeystorePath()), true);
        Credentials credentials = WalletUtils.loadCredentials(password, bankConfig.getKeystorePath() + "/" + filename);
        String address = credentials.getAddress();
        
        AccountEntity accountEntity = AccountEntity.builder().name(name).password(password).filename(filename).address(address).balance(BigDecimal.ZERO).build();
        return accountRepository.save(accountEntity);
    }

    @Override
    public boolean update(AccountEntity accountEntity) {
        accountRepository.save(accountEntity); // todo, should check
        return true;
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
