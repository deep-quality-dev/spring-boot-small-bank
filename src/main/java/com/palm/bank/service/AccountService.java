package com.palm.bank.service;

import com.palm.bank.entity.AccountEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AccountService extends UserDetailsService {

    /**
     * Save and update account entity in `accounts` table
     *
     * @param accountEntity
     * @return
     */
    boolean save(AccountEntity accountEntity);

    /**
     * Find all the accounts in `accounts` table
     *
     * @return
     */
    List<AccountEntity> findAll();

    /**
     * Find the account by `id` in `accounts` table
     *
     * @param accountId
     * @return
     */
    AccountEntity findById(Long accountId);

    /**
     * Find the account by `name` in `accounts` table
     *
     * @param name
     * @return
     */
    AccountEntity findByName(String name);

    /**
     * Find the account by `address` in `accounts` table
     *
     * @param address
     * @return
     */
    AccountEntity findByAddress(String address);
}
