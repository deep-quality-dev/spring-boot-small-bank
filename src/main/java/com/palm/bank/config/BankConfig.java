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

    /**
     * Fee in percent, 100% in 10000
     */
    private Integer feeInPercent;

    /**
     * Increase gas fee per every transactions
     */
    private Integer gasSpeedUp;

    /**
     * Gas limit
     */
    private BigInteger gasLimit;

    /**
     * Keystore path to withdraw wallet
     */
    private String withdrawWalletKeystore;

    /**
     * Password to keystore file of withdraw wallet
     */
    private String withdrawWalletPassword;

    public Credentials getWithdrawWallet() throws IOException, CipherException {
        return WalletUtils.loadCredentials(withdrawWalletPassword, withdrawWalletKeystore);
    }
}
