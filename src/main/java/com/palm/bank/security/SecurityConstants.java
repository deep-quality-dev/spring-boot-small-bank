package com.palm.bank.security;

import com.palm.bank.SpringApplicationContext;
import com.palm.bank.config.BankConfig;

public class SecurityConstants {

    public static final long EXPIRATION_TIME = 864000000; // 10 days

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String BANK_URL = "/v1/bank";
    public static final String SIGNUP_URL = "/v1/bank/create-account";
    public static final String H2_CONSOLE = "/h2-console/**";

    public static String getTokenSecret() {
        BankConfig bankConfig = (BankConfig) SpringApplicationContext.getBean("bankConfig");
        return bankConfig.getTokenSecret();
    }
}
