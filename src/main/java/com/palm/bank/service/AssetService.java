package com.palm.bank.service;

import com.palm.bank.entity.AccountEntity;
import org.springframework.stereotype.Service;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;

@Service
public interface AssetService {

    /**
     * Get the balance by address in bank
     * @param address
     * @return account balance
     * @throws IOException
     */
    BigDecimal getBalance(String address) throws IOException;

    /**
     * Create new wallet address and add it in bank
     * @param name new user name
     * @param password new user password
     * @return account entity with new wallet address
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidAlgorithmParameterException
     * @throws CipherException
     * @throws IOException
     */
    AccountEntity createNewWallet(String name, String password) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CipherException, IOException;

    /**
     * Move balance in bank, cutting off the fee
     * @param from
     * @param to
     * @param amount
     * @param fee
     * @return
     */
    String internalTransfer(AccountEntity from, AccountEntity to, BigDecimal amount, BigDecimal fee);

    /**
     * Deposit tokens, refer: BankController.deposit
     * @param from
     * @param amount
     * @return
     */
    String deposit(AccountEntity from, BigDecimal amount);

    /**
     * Withdraw tokens, refer: BankController.withdraw
     * @param to
     * @param amount
     * @param fee
     * @return
     */
    String withdraw(AccountEntity to, BigDecimal amount, BigDecimal fee);
}
