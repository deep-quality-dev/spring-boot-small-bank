package com.palm.bank.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public interface EtherService {

    BigDecimal getBalance(String account) throws IOException;

    boolean transfer(String from, String to, BigDecimal amount);
}
