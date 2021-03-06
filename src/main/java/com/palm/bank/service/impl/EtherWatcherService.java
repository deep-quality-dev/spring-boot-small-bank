package com.palm.bank.service.impl;

import com.palm.bank.config.BankConfig;
import com.palm.bank.entity.TransactionEntity;
import com.palm.bank.event.DepositEvent;
import com.palm.bank.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.Transaction;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class EtherWatcherService {

    private final BankConfig bankConfig;

    private final Credentials withdrawWallet;

    private final Web3j web3j;

    private final AssetService assetService;

    private final DepositEvent depositEvent;

    private Long currentBlockNumber = 0L;
    private int step = 5;

    public EtherWatcherService(BankConfig bankConfig, Credentials withdrawWallet, Web3j web3j, AssetService assetService, DepositEvent depositEvent) {
        this.bankConfig = bankConfig;
        this.withdrawWallet = withdrawWallet;
        this.web3j = web3j;
        this.assetService = assetService;
        this.depositEvent = depositEvent;

        this.currentBlockNumber = getBlockNumber();

        log.info("withdraw wallet = {}", this.withdrawWallet.getAddress());
    }

    @Scheduled(fixedDelay = 2000)
    public void check() {
        try {
            long blockNumber = getBlockNumber();
            long startBlockNumber = currentBlockNumber + 1;
            currentBlockNumber = blockNumber - currentBlockNumber > step ? currentBlockNumber + step : blockNumber;

            List<TransactionEntity> deposits = replayBlock(startBlockNumber, currentBlockNumber);
            if (deposits != null) {
                for (TransactionEntity deposit : deposits) {
                    depositEvent.onConfirmed(deposit);
                }
            }
        } catch (Exception ex) {
            log.error(ex.toString());
        }
    }

    /**
     * Find all the deposit events within blocks
     *
     * @param startBlockNumber
     * @param endBlockNumber
     * @return
     */
    private List<TransactionEntity> replayBlock(long startBlockNumber, long endBlockNumber) {
        List<TransactionEntity> deposits = new ArrayList<>();
        try {
            for (long blockNumber = startBlockNumber; blockNumber <= endBlockNumber; blockNumber++) {
                EthBlock block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true).send();

                block.getBlock().getTransactions().stream().forEach(transactionResult -> {
                    EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                    Transaction transaction = transactionObject.get();
                    // Check if transaction is transferring tokens to withdraw wallet
                    if (transaction.getTo() != null && transaction.getTo().equalsIgnoreCase(withdrawWallet.getAddress())) {
                        // Add deposit events
                        TransactionEntity transactionEntity = TransactionEntity.builder()
                                .txHash(transaction.getHash())
                                .blockNumber(transaction.getBlockNumber().longValue())
                                .blockHash(transaction.getBlockHash())
                                .amount(transaction.getValue().toString())
                                .fromAddress(transaction.getFrom())
                                .toAddress(withdrawWallet.getAddress())
                                .build();
                        deposits.add(transactionEntity);
                        log.info("received deposit at block: txHash={}, from={}, to={}, amount={}, blockNumber={}",
                                transactionEntity.getTxHash(),
                                transactionEntity.getFromAddress(),
                                transactionEntity.getToAddress(),
                                transactionEntity.getAmount(),
                                transactionEntity.getBlockNumber());
                    }
                });
            }
        } catch (Exception ex) {
            log.error(ex.toString());
        }
        return deposits;
    }

    private Long getBlockNumber() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber().longValue();
        } catch (Exception ex) {
            return 0L;
        }
    }
}
