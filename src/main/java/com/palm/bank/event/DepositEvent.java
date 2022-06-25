package com.palm.bank.event;

import com.palm.bank.entity.DepositEntity;
import com.palm.bank.service.DepositService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DepositEvent {

    @Autowired
    private final DepositService depositService;

    public DepositEvent(DepositService depositService) {
        this.depositService = depositService;
    }

    public synchronized void onConfirmed(DepositEntity depositEntity) {
        if (depositService.exists(depositEntity)) {
            log.info("confirmed deposit: txHash={}, address={}, amount={}", depositEntity.getTxHash(), depositEntity.getAddress(), depositEntity.getAmount().toString());
            depositService.save(depositEntity);
        }
    }
}
