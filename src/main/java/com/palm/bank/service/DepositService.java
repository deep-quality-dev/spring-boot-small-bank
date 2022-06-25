package com.palm.bank.service;

import com.palm.bank.entity.DepositEntity;
import org.springframework.stereotype.Service;

@Service
public interface DepositService {

    boolean exists(DepositEntity depositEntity);

    void save(DepositEntity depositEntity);
}
