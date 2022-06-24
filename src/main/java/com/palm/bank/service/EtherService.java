package com.palm.bank.service;

import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Service
public interface EtherService {
    
    String createNewWallet(String name, String password) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException;

    BigDecimal getBalance(String account) throws IOException;

    boolean transfer(String from, String to, BigDecimal amount);
}
