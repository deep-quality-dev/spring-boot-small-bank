package com.palm.bank.component;

import com.palm.bank.config.BankConfig;
import com.palm.bank.entity.DepositEntity;
import com.palm.bank.event.DepositEvent;
import com.palm.bank.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.Transaction;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class EtherWatcher implements Runnable {

    @Autowired
    private final BankConfig bankConfig;

    @Autowired
    private final Web3j web3j;

    @Autowired
    private final AssetService assetService;

    @Autowired
    private final DepositEvent depositEvent;

    private final int CHECK_INTERVAL = 2000;

    private String withdrawWallet;

    private Long currentBlockNumber = 0L;
    private int step = 5;

    public EtherWatcher(BankConfig bankConfig, Web3j web3j, AssetService assetService, DepositEvent depositEvent) {
        this.bankConfig = bankConfig;
        this.web3j = web3j;
        this.assetService = assetService;
        this.depositEvent = depositEvent;

        try {
            this.withdrawWallet = bankConfig.getWithdrawWallet().getAddress();
        } catch (Exception ex) {
            this.withdrawWallet = null;
        }
        log.info("withdraw wallet = {}", this.withdrawWallet);
    }

    public void setCurrentBlockNumber(Long blockNumber) {
        this.currentBlockNumber = blockNumber;
    }

    @Override
    public void run() {
        long nextCheck = 0;
        while (!Thread.interrupted()) {
            if (nextCheck <= System.currentTimeMillis()) {
                try {
                    nextCheck = System.currentTimeMillis() + CHECK_INTERVAL;
                    check();
                } catch (Exception ex) {
                }
            } else {
                try {
                    Thread.sleep(Math.max(System.currentTimeMillis() - nextCheck, 100));
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public void check() {
        try {
            long blockNumber = getBlockNumber();
            long startBlockNumber = currentBlockNumber + 1;
            currentBlockNumber = blockNumber - currentBlockNumber > step ? currentBlockNumber + step : blockNumber;

            List<DepositEntity> deposits = replayBlock(startBlockNumber, currentBlockNumber);
            if (deposits != null) {
                for (DepositEntity deposit : deposits) {
                    depositEvent.onConfirmed(deposit);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private List<DepositEntity> replayBlock(long startBlockNumber, long endBlockNumber) {
        List<DepositEntity> deposits = new ArrayList<>();
        try {
            for (long blockNumber = startBlockNumber; blockNumber <= endBlockNumber; blockNumber++) {
                EthBlock block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true).send();

                block.getBlock().getTransactions().stream().forEach(transactionResult -> {
                    EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                    Transaction transaction = transactionObject.get();
                    if (transaction.getTo() != null && transaction.getTo().equalsIgnoreCase(withdrawWallet)) {
                        DepositEntity depositEntity = DepositEntity.builder()
                                .txHash(transaction.getHash())
                                .blockNumber(transaction.getBlockNumber().longValue())
                                .blockHash(transaction.getBlockHash())
                                .amount(transaction.getValue().toString())
                                .address(transaction.getFrom())
                                .build();
                        deposits.add(depositEntity);
                        log.info("received deposit at block: txHash={}, from={}, amount={}, blockNumber={}",
                                depositEntity.getTxHash(),
                                depositEntity.getAddress(),
                                depositEntity.getAmount(),
                                depositEntity.getBlockNumber());
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return deposits;
    }

    public Long getBlockNumber() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber().longValue();
        } catch (Exception ex) {
            return 0L;
        }
    }
}
