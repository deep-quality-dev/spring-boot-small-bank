package com.palm.bank.service;

import com.palm.bank.config.BankConfig;
import com.palm.bank.entity.AccountEntity;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssetServiceTests {

    @Autowired
    private BankConfig bankConfig;

    @Autowired
    private Credentials withdrawWallet;

    @MockBean
    private AccountService accountService;

    @MockBean
    private Web3j web3j;

    @Autowired
    private AssetService assetService;

    @Test
    public void createNewWallet() throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        doReturn(true)
                .when(accountService)
                .save(any(AccountEntity.class));

        AccountEntity accountEntity = assetService.createNewWallet("name", "password");
        Assert.assertNotNull(accountEntity);
        Assert.assertEquals(accountEntity.getName(), "name");
        Assert.assertNotNull(accountEntity.getFilename());
        Assert.assertNotNull(accountEntity.getAddress());
        Files.deleteIfExists(Paths.get(bankConfig.getWallet().getKeystorePath() + "/" + accountEntity.getFilename()));
    }

    @Test
    public void internalTransfer() {
        doReturn(true)
                .when(accountService)
                .save(any(AccountEntity.class));

        AccountEntity from = AccountEntity.builder()
                .name("name")
                .encodedPassword("password")
                .address("address")
                .filename("filename")
                .balance(BigDecimal.TEN.pow(18).toString())
                .build();

        AccountEntity to = AccountEntity.builder()
                .name("name1")
                .encodedPassword("password1")
                .address("address1")
                .filename("filename1")
                .balance(BigDecimal.TEN.pow(18).multiply(new BigDecimal(2)).toString())
                .build();

        String hash = assetService.internalTransfer(from, to, BigDecimal.TEN.pow(18), BigDecimal.ZERO);
        Assert.assertEquals(from.getBalance(), BigDecimal.ZERO);
        Assert.assertEquals(to.getBalance(), BigDecimal.TEN.pow(18).multiply(new BigDecimal(3)));
        Assert.assertNotNull(hash);
    }

    private void mockTransfer(String from, String mockTxHash, Function<String, Void> callback) throws IOException {
        // Mock GetTransactionCount
        Request mockRequestEthGetTransactionCount = mock(Request.class);
        EthGetTransactionCount mockResponseEthGetTransactionCount = mock(EthGetTransactionCount.class);
        BigInteger nonce = BigInteger.ONE;

        doReturn(mockRequestEthGetTransactionCount)
                .when(web3j)
                .ethGetTransactionCount(from, DefaultBlockParameterName.LATEST);
        when(mockRequestEthGetTransactionCount.sendAsync()).thenReturn(CompletableFuture.completedFuture(mockResponseEthGetTransactionCount));
        when(mockResponseEthGetTransactionCount.getTransactionCount()).thenReturn(nonce);

        // Mock GasPrice
        Request mockRequestGetPrice = mock(Request.class);
        EthGasPrice mockResponseEthGetPrice = mock(EthGasPrice.class);
        BigInteger gasPrice = BigInteger.TEN;

        doReturn(mockRequestGetPrice)
                .when(web3j)
                .ethGasPrice();
        when(mockRequestGetPrice.send()).thenReturn(mockResponseEthGetPrice);
        when(mockResponseEthGetPrice.getGasPrice()).thenReturn(gasPrice);

        Request mockRequestSendTransaction = mock(Request.class);
        EthSendTransaction mockResponseEthSendTransaction = mock(EthSendTransaction.class);

        doReturn(mockRequestSendTransaction)
                .when(web3j)
                .ethSendRawTransaction(any(String.class));
        when(mockRequestSendTransaction.sendAsync()).thenReturn(CompletableFuture.completedFuture(mockResponseEthSendTransaction));
        when(mockResponseEthSendTransaction.getTransactionHash()).thenReturn(mockTxHash);

        callback.apply(mockTxHash);
    }

    @Test
    public void deposit() throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        doReturn(true)
                .when(accountService)
                .save(any(AccountEntity.class));
        AccountEntity fromAccount = assetService.createNewWallet("name", "password");

        try {
            mockTransfer(fromAccount.getAddress(), "txHash", (mockTxHash) -> {
                String txHash = assetService.deposit(fromAccount, BigDecimal.TEN);
                Assert.assertEquals(txHash, mockTxHash);
                return null;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Files.deleteIfExists(Paths.get(bankConfig.getWallet().getKeystorePath() + "/" + fromAccount.getFilename()));
        }
    }

    @Test
    public void withdraw() throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        doReturn(true)
                .when(accountService)
                .save(any(AccountEntity.class));
        AccountEntity fromAccount = assetService.createNewWallet("name", "password");

        try {
            fromAccount.setBalance(BigDecimal.TEN.add(BigDecimal.ONE));

            mockTransfer(withdrawWallet.getAddress(), "txHash", (mockTxHash) -> {
                String txHash = assetService.withdraw(fromAccount, BigDecimal.TEN, BigDecimal.ONE);
                Assert.assertEquals(txHash, mockTxHash);

                Assert.assertEquals(fromAccount.getBalance(), BigDecimal.ZERO);
                return null;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Files.deleteIfExists(Paths.get(bankConfig.getWallet().getKeystorePath() + "/" + fromAccount.getFilename()));
        }
    }
}
