package com.palm.bank.service;

import com.palm.bank.entity.AccountTokenEntity;
import org.springframework.stereotype.Service;

@Service
public interface LoginService {

    AccountTokenEntity login(String name, String password);

    AccountTokenEntity isValid(String token);
}
