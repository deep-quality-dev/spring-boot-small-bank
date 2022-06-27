package com.palm.bank.service;

import com.palm.bank.config.BankConfig;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.AccountTokenEntity;
import com.palm.bank.repository.AccountTokenRepository;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.web3j.crypto.CipherException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
public class LoginServiceTests {

    @Autowired
    private BankConfig bankConfig;

    @MockBean
    private AccountService accountService;

    @Autowired
    private AssetService assetService;

    @MockBean
    private AccountTokenRepository accountTokenRepository;

    @Autowired
    private LoginService loginService;

    @Test
    public void login() throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        doReturn(true)
                .when(accountService)
                .save(any(AccountEntity.class));

        String name = "name", password = "password";
        AccountEntity accountEntity = assetService.createNewWallet(name, password);
        Assert.assertNotNull(accountEntity);
        Assert.assertEquals(accountEntity.getName(), name);
        Assert.assertNotNull(accountEntity.getFilename());
        Assert.assertNotNull(accountEntity.getAddress());
        Files.deleteIfExists(Paths.get(bankConfig.getKeystorePath() + "/" + accountEntity.getFilename()));

        doReturn(accountEntity)
                .when(accountService)
                .findByName(name);
        doReturn(new AccountTokenEntity())
                .when(accountTokenRepository)
                .save(any(AccountTokenEntity.class));
        doReturn(new AccountTokenEntity())
                .when(accountTokenRepository)
                .findByToken(any(String.class));

        AccountTokenEntity accountTokenEntity = loginService.login(name, password);
        Assert.assertEquals(accountTokenEntity.getAccountId(), accountEntity.getId());
        Assert.assertNotNull(accountTokenEntity.getToken());

        Files.deleteIfExists(Paths.get(bankConfig.getKeystorePath() + "/" + accountEntity.getFilename()));
    }
}
