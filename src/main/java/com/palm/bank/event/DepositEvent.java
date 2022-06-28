package com.palm.bank.event;

import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.TransactionEntity;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class DepositEvent {

    @Autowired
    private final TransactionService transactionService;

    @Autowired
    private final AccountService accountService;

    public DepositEvent(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    /**
     * Ether was already transferred to withdraw wallet, update the balance of same account
     * @param transactionEntity
     */
    public synchronized void onConfirmed(TransactionEntity transactionEntity) {
        if (!transactionService.exists(transactionEntity)) {
            log.info("confirmed deposit: txHash={}, from address={}, to address={}, amount={}",
                    transactionEntity.getTxHash(),
                    transactionEntity.getFromAddress(),
                    transactionEntity.getToAddress(),
                    transactionEntity.getAmount());
            transactionService.save(transactionEntity);

            AccountEntity accountEntity = accountService.findByAddress(transactionEntity.getFromAddress());
            if (accountEntity != null) {
                accountEntity.setBalance(accountEntity.getBalance().add(new BigDecimal(transactionEntity.getAmount())));
                accountService.save(accountEntity);
                log.info("updated account balance: address={}, amount={}", accountEntity.getAddress(), accountEntity.getBalance());
            }
        }
    }
}
