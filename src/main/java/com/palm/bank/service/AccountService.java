package com.palm.bank.service;

import com.palm.bank.entity.AccountEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AccountService {

    boolean save(AccountEntity accountEntity);

    List<AccountEntity> findAll();

    AccountEntity findById(Long accountId);

    AccountEntity findByName(String name);

    AccountEntity findByAddress(String address);
}
