package com.palm.bank.service.impl;

import com.palm.bank.config.BankConfig;
import com.palm.bank.entity.DepositEntity;
import com.palm.bank.repository.AccountRepository;
import com.palm.bank.repository.DepositRepository;
import com.palm.bank.service.DepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("depositService")
public class DepositServiceImpl implements DepositService {

    @Autowired
    private final DepositRepository depositRepository;

    public DepositServiceImpl(DepositRepository depositRepository) {
        this.depositRepository = depositRepository;
    }

    @Override
    public boolean exists(DepositEntity depositEntity) {
        DepositEntity found = depositRepository.findByHash(depositEntity.getTxHash());
        return found != null ? true : false;
    }

    @Override
    public void save(DepositEntity depositEntity) {
        depositRepository.save(depositEntity);
    }
}
