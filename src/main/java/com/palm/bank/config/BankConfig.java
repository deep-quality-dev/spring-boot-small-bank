package com.palm.bank.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.IOException;
import java.math.BigInteger;

@Data
@EnableScheduling
@Configuration
@ConfigurationProperties(prefix = "bank")
public class BankConfig {

    /**
     * Signing key for jwt token
     */
    private String tokenSecret;

    /**
     * Fee in percent, 100% in 10000
     */
    private Integer feeInPercent;

    private HttpConfig http;

    private GasConfig gas;

    private WalletConfig wallet;

    @Bean("withdrawWallet")
    public Credentials getWithdrawWallet(BankConfig bankConfig) throws IOException, CipherException {
        return WalletUtils.loadCredentials(bankConfig.getWallet().getWithdrawWalletPassword(),
                bankConfig.getWallet().getWithdrawWalletKeystore());
    }

    @Data
    public static class HttpConfig {
        /**
         * RPC url of endpoint node
         */
        private String rpc;

        /**
         * Connect timeout to http connection
         */
        private Integer connectTimeout;

        /**
         * Read timeout to http connection
         */
        private Integer readTimeout;

        /**
         * Write timeout to http connection
         */
        private Integer writeTimeout;
    }

    @Data
    public static class GasConfig {
        /**
         * Increase gas fee per every transactions
         */
        private Integer speedUp;

        /**
         * Gas limit
         */
        private BigInteger limit;
    }

    @Data
    public static class WalletConfig {
        /**
         * Keystore path to all the wallets
         */
        private String keystorePath;

        /**
         * Keystore path to withdraw wallet
         */
        private String withdrawWalletKeystore;

        /**
         * Password to keystore file of withdraw wallet
         */
        private String withdrawWalletPassword;
    }
}
