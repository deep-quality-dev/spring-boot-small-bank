package com.palm.bank.service.impl;

import com.palm.bank.config.BankConfig;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.EtherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Service("etherService")
public class EtherServiceImpl implements EtherService {

    @Autowired
    private final BankConfig bankConfig;

    @Autowired
    private final AccountService accountService;

    @Autowired
    private final Web3j web3j;

    public EtherServiceImpl(BankConfig bankConfig, AccountService accountService, Web3j web3j) {
        this.bankConfig = bankConfig;
        this.accountService = accountService;
        this.web3j = web3j;
    }

    @Override
    public String createNewWallet(String name, String password) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException {
        String filename = WalletUtils.generateNewWalletFile(password, new File(bankConfig.getKeystorePath()), true);
        Credentials credentials = WalletUtils.loadCredentials(password, bankConfig.getKeystorePath() + "/" + filename);
        String address = credentials.getAddress();
        accountService.saveOne(name, password, address);
        return address;
    }

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
