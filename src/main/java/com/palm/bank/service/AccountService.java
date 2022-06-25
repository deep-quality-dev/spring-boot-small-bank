package com.palm.bank.service;

import com.palm.bank.entity.AccountEntity;
import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Service
public interface AccountService {

    AccountEntity saveOne(String name, String password) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException;
    
    boolean update(AccountEntity accountEntity);

    AccountEntity findById(Long accountId);

    AccountEntity findByName(String name);
    
    AccountEntity findByAddress(String address);
}
