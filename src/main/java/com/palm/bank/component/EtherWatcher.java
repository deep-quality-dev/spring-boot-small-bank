package com.palm.bank.component;

import com.palm.bank.common.Unit;
import com.palm.bank.entity.DepositEntity;
import com.palm.bank.event.DepositEvent;
import com.palm.bank.util.EtherConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
    private final Web3j web3j;

    @Autowired
    private final DepositEvent depositEvent;

    private final int CHECK_INTERVAL = 2000;

    private Long currentBlockNumber = 0L;
    private int step = 5;

    public EtherWatcher(Web3j web3j, DepositEvent depositEvent) {
        this.web3j = web3j;
        this.depositEvent = depositEvent;
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
            log.info("check blocks: {} - {}", startBlockNumber, currentBlockNumber);
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
                    if (transaction.getTo() != null && transaction.getTo().length() > 0) {
                        DepositEntity depositEntity = DepositEntity.builder()
                                .txHash(transaction.getHash())
                                .blockNumber(transaction.getBlockNumber().longValue())
                                .blockHash(transaction.getBlockHash())
                                .amount(EtherConvert.fromWei(transaction.getValue().toString(), Unit.ETHER))
                                .build();
                        deposits.add(depositEntity);
                        log.info("received deposit at block: txHash={}, amount={}, blockNumber={}",
                                depositEntity.getTxHash(),
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
