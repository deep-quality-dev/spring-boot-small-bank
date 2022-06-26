package com.palm.bank.event;

import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.DepositEntity;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.DepositService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public synchronized void onConfirmed(DepositEntity depositEntity) {
        if (!depositService.exists(depositEntity)) {
            log.info("confirmed deposit: txHash={}, address={}, amount={}", depositEntity.getTxHash(), depositEntity.getAddress(), depositEntity.getAmount().toString());
//            depositService.save(depositEntity);

            AccountEntity accountEntity = accountService.findByAddress(depositEntity.getAddress());
            if (accountEntity != null) {
                log.info("updated account balance: address={}, amount={}", accountEntity.getAddress(), accountEntity.getBalance());
                accountEntity.setBalance(accountEntity.getBalance().add(depositEntity.getAmount()));
                accountService.save(accountEntity);
            }
        }
    }
}
