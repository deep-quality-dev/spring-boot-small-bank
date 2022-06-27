package com.palm.bank.event;

import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.DepositEntity;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.DepositService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class DepositEvent {

    @Autowired
    private final DepositService depositService;

    @Autowired
    private final AccountService accountService;

    public DepositEvent(DepositService depositService, AccountService accountService) {
        this.depositService = depositService;
        this.accountService = accountService;
    }

    /**
     * Ether was already transferred to withdraw wallet, update the balance of same account
     * @param depositEntity
     */
    public synchronized void onConfirmed(DepositEntity depositEntity) {
        if (!depositService.exists(depositEntity)) {
            log.info("confirmed deposit: txHash={}, address={}, amount={}", depositEntity.getTxHash(), depositEntity.getAddress(), depositEntity.getAmount().toString());
            depositService.save(depositEntity);

            AccountEntity accountEntity = accountService.findByAddress(depositEntity.getAddress());
            if (accountEntity != null) {
                accountEntity.setBalance(accountEntity.getBalance().add(new BigDecimal(depositEntity.getAmount())));
                accountService.save(accountEntity);
                log.info("updated account balance: address={}, amount={}", accountEntity.getAddress(), accountEntity.getBalance());
            }
        }
    }
}
