package com.palm.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class PalmSmallBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(PalmSmallBankApplication.class, args);
    }

}
