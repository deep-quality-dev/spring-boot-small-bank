package com.palm.bank.service.impl;

import com.palm.bank.common.Unit;
import com.palm.bank.service.EtherService;
import com.palm.bank.util.EtherConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@Service("etherService")
public class EtherServiceImpl implements EtherService {

    @Autowired
    private Web3j web3j;

    @Override
    public BigDecimal getBalance(String account) throws IOException {
        EthGetBalance balance = web3j.ethGetBalance(account, DefaultBlockParameterName.LATEST).send();
        return new BigDecimal(balance.getBalance().toString());
    }

    @Override
    public boolean transfer(String from, String to, BigDecimal amount) {
        return false;
    }
}
