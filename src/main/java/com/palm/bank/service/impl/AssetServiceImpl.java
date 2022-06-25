package com.palm.bank.service.impl;

import com.palm.bank.config.BankConfig;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service("assetService")
public class AssetServiceImpl implements AssetService {

    @Autowired
    private final BankConfig bankConfig;

    @Autowired
    private final Web3j web3j;

    public AssetServiceImpl(BankConfig bankConfig, Web3j web3j) {
        this.bankConfig = bankConfig;
        this.web3j = web3j;
    }

    @Override
    public BigDecimal getBalance(String account) throws IOException {
        EthGetBalance balance = web3j.ethGetBalance(account, DefaultBlockParameterName.LATEST).send();
        return new BigDecimal(balance.getBalance().toString());
    }

    public BigInteger getGasPrice() throws IOException {
        EthGasPrice gasPrice = web3j.ethGasPrice().send();
        BigInteger baseGasPrice = gasPrice.getGasPrice();
        BigInteger pow4 = BigInteger.TEN.pow(4);
        return baseGasPrice.multiply(new BigInteger(bankConfig.getGasSpeedUp().toString()).add(pow4)).divide(pow4);
    }

    @Override
    public String transfer(AccountEntity from, AccountEntity to, BigDecimal amount, BigDecimal fee) {
        from.setBalance(from.getBalance().subtract(amount.add(fee)));
        // todo, generate txHash
        return "txHash";
    }

    @Override
    public String withdraw(AccountEntity to, BigDecimal amount, BigDecimal fee) {
        try {
            Credentials credentials = WalletUtils.loadCredentials(bankConfig.getWithdrawWalletPassword(), bankConfig.getKeystorePath() + "/" + bankConfig.getWithdrawWalletKeystore());

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            BigInteger gasPrice = this.getGasPrice();
            BigInteger gasLimit = bankConfig.getGasLimit();

            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to.getAddress(), amount.toBigInteger());
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            String transactionHash = ethSendTransaction.getTransactionHash();
            log.info("ransfer ether: txid = {}", transactionHash);
            return transactionHash;

        } catch (Exception ex) {
            log.error(ex.toString());
        }
        return null;
    }
}
