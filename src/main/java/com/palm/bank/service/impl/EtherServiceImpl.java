package com.palm.bank.service.impl;

import com.palm.bank.service.EtherService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service("etherService")
public class EtherServiceImpl implements EtherService {

    @Override
    public BigInteger getBalance(String account) {
        return null;
    }

    @Override
    public boolean transfer(String from, String to, BigDecimal amount) {
        return false;
    }
}
