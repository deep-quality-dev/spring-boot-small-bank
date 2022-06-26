package com.palm.bank.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.IOException;
import java.math.BigInteger;

@Data
@Component
@ConfigurationProperties(prefix = "bank")
public class BankConfig {

    private String rpc;

    private String keystorePath;

    private Integer feeInPercent;

    private Integer gasSpeedUp;
    
    private BigInteger gasLimit;

    private String withdrawWalletKeystore;

    private String withdrawWalletPassword;

    public Credentials getWithdrawWallet() throws IOException, CipherException {
        return WalletUtils.loadCredentials(withdrawWalletPassword, keystorePath + "/" + withdrawWalletKeystore);
    }
}
