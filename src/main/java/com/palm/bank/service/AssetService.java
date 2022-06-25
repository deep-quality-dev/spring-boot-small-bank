package com.palm.bank.service;

import com.palm.bank.entity.AccountEntity;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public interface AssetService {

    BigDecimal getBalance(String account) throws IOException;

    String transfer(AccountEntity from, AccountEntity to, BigDecimal amount, BigDecimal fee);

    String withdraw(AccountEntity to, BigDecimal amount, BigDecimal fee);
}
