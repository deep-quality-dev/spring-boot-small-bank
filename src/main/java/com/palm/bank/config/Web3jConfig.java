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
    @ConditionalOnProperty(name = "bank.wallet.keystore-path")
    public Web3j web3j(BankConfig config) {
        log.info("rpc: {}", config.getHttp().getRpc());

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(config.getHttp().getConnectTimeout(), TimeUnit.MILLISECONDS);
        builder.readTimeout(config.getHttp().getReadTimeout(), TimeUnit.MILLISECONDS);
        builder.writeTimeout(config.getHttp().getWriteTimeout(), TimeUnit.MILLISECONDS);
        OkHttpClient client = builder.build();
        return Web3j.build(new HttpService(config.getHttp().getRpc(), client, false));
    }
}
