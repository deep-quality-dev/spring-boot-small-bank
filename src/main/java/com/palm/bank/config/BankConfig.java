package com.palm.bank.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
}
