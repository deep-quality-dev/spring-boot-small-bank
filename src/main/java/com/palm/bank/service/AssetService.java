package com.palm.bank.service;

import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Service
public interface AssetService {
    
    BigDecimal getBalance(String address);
}
