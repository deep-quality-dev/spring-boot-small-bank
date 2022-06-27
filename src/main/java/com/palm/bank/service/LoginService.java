package com.palm.bank.service;

import com.palm.bank.entity.AccountTokenEntity;
import org.springframework.stereotype.Service;

@Service
public interface LoginService {

    /**
     * Login to bank with user name and password
     * @param name
     * @param password
     * @return account id, new token, expire time
     */
    AccountTokenEntity login(String name, String password);

    /**
     * Check if token is valid or expired
     * @param token
     * @return
     */
    AccountTokenEntity isValid(String token);
}
