package com.palm.bank.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public interface EtherService {

    BigInteger getBalance(String account);

    boolean transfer(String from, String to, BigDecimal amount);
}
