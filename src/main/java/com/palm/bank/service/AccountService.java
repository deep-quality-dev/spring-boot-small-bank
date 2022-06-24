package com.palm.bank.service;

import com.palm.bank.entity.AccountEntity;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {

    AccountEntity saveOne(String name, String password, String address);

    AccountEntity findByName(String name);
}
