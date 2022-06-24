package com.palm.bank.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class Web3jConfig {

    @Bean("web3j")
    @ConditionalOnProperty(name = "bank.keystore-path")
    public Web3j web3j(BankConfig config) {
        log.info("rpc: {}", config.getRpc());

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30 * 1000, TimeUnit.MILLISECONDS);
        builder.readTimeout(30 * 1000, TimeUnit.MILLISECONDS);
        builder.writeTimeout(30 * 1000, TimeUnit.MILLISECONDS);
        OkHttpClient client = builder.build();
        return Web3j.build(new HttpService(config.getRpc(), client, false));
    }
}
