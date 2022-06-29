package com.palm.bank.service.impl;

import com.palm.bank.config.BankConfig;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.TransactionEntity;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.AssetService;
import com.palm.bank.service.TransactionService;
import com.palm.bank.util.TokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service("assetService")
public class AssetServiceImpl implements AssetService {

    @Autowired
    private final BankConfig bankConfig;

    @Autowired
    private final Web3j web3j;

    @Autowired
    private final AccountService accountService;

    @Autowired
    private final TransactionService transactionService;

    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AssetServiceImpl(BankConfig bankConfig, Web3j web3j, AccountService accountService, TransactionService transactionService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bankConfig = bankConfig;
        this.web3j = web3j;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public BigDecimal getBalance(String address) throws IOException {
        EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        return new BigDecimal(balance.getBalance().toString());
    }

    public BigInteger getGasPrice() throws IOException {
        EthGasPrice gasPrice = web3j.ethGasPrice().send();
        BigInteger baseGasPrice = gasPrice.getGasPrice();
        BigInteger pow4 = BigInteger.TEN.pow(4);
        return baseGasPrice.multiply(new BigInteger(bankConfig.getGasSpeedUp().toString()).add(pow4)).divide(pow4);
    }

    @Override
    public AccountEntity createNewWallet(String name, String password) {
        try {
            String encoded = bCryptPasswordEncoder.encode(password);

            String filename = WalletUtils.generateNewWalletFile(encoded, new File(bankConfig.getKeystorePath()), true);
            Credentials credentials = WalletUtils.loadCredentials(encoded, bankConfig.getKeystorePath() + "/" + filename);
            String address = credentials.getAddress();

            AccountEntity accountEntity =
                    AccountEntity.builder()
                            .name(name)
                            .encodedPassword(encoded) // Password should be stored as encrypted
                            .filename(filename)
                            .address(address)
                            .balance(BigDecimal.ZERO.toString())
                            .build();

            log.info("new account: name={}, filename={}, address={}, password={}, encoded={}", name, filename, address, password, encoded);
            if (accountService.save(accountEntity)) {
                return accountEntity;
            }
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String internalTransfer(AccountEntity from, AccountEntity to, BigDecimal amount, BigDecimal fee) {
        from.setBalance(from.getBalance().subtract(amount.add(fee)));
        to.setBalance(to.getBalance().add(amount));
        accountService.save(from);
        accountService.save(to);

        String txHash = TokenGenerator.generate(String.valueOf(new Date().getTime()));
        transactionService.save(TransactionEntity.builder()
                .txHash(txHash)
                .amount(amount.toString())
                .fromAddress(from.getAddress())
                .toAddress(to.getAddress())
                .build());
        return txHash;
    }

    private String transfer(Credentials credentials, String to, BigDecimal amount) throws IOException, ExecutionException, InterruptedException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        BigInteger gasPrice = this.getGasPrice();
        BigInteger gasLimit = bankConfig.getGasLimit();

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, amount.toBigInteger());
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        String transactionHash = ethSendTransaction.getTransactionHash();
        return transactionHash;
    }

    @Override
    public String deposit(AccountEntity from, BigDecimal amount) {
        try {
            Credentials credentials = WalletUtils.loadCredentials(from.getEncodedPassword(), bankConfig.getKeystorePath() + "/" + from.getFilename());

            String transactionHash = transfer(credentials, bankConfig.getWithdrawWallet().getAddress(), amount);
            log.info("deposit ether: from address={}, txHash = {}", credentials.getAddress(), transactionHash);
            return transactionHash;
        } catch (Exception ex) {
            ex.printStackTrace();

            return null;
        }
    }

    @Override
    public synchronized String withdraw(AccountEntity to, BigDecimal amount, BigDecimal fee) {
        try {
            Credentials credentials = bankConfig.getWithdrawWallet();

            // Subtract balance for pending
            to.setBalance(to.getBalance().subtract(amount.add(fee)));
            accountService.save(to);

            String transactionHash = transfer(credentials, to.getAddress(), amount);
            log.info("withdraw ether: txHash = {}", transactionHash);

            transactionService.save(TransactionEntity.builder()
                    .txHash(transactionHash)
                    .amount(amount.toString())
                    .fromAddress(credentials.getAddress())
                    .toAddress(to.getAddress())
                    .build());

            return transactionHash;
        } catch (Exception ex) {
            ex.printStackTrace();

            // Recover balance if failed
            to.setBalance(to.getBalance().add(amount.add(fee)));
            accountService.save(to);
        }
        return null;
    }
}
