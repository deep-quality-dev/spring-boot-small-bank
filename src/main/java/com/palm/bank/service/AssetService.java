package com.palm.bank.service;

import com.palm.bank.entity.AccountEntity;
import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

@Service
public interface AssetService {

    List<String> getUnlockedAccounts() throws IOException;

    BigDecimal getBalance(String address) throws IOException;

    AccountEntity createNewWallet(String name, String password) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException;

    String internalTransfer(AccountEntity from, AccountEntity to, BigDecimal amount, BigDecimal fee);

    String deposit(AccountEntity from, BigDecimal amount);

    String withdraw(AccountEntity to, BigDecimal amount, BigDecimal fee);
}
