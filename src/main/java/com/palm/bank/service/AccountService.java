package com.palm.bank.service;

import com.palm.bank.entity.AccountEntity;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {

    AccountEntity create(String name, String password);

    AccountEntity findByName(String name);
}
