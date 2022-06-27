package com.palm.bank.service;

import com.palm.bank.entity.DepositEntity;
import org.springframework.stereotype.Service;

@Service
public interface DepositService {

    /**
     * Check if deposit transaction was already registered in `deposits` table
     * @param depositEntity
     * @return
     */
    boolean exists(DepositEntity depositEntity);

    /**
     * Save deposit transaction in `deposits` table
     * @param depositEntity
     * @return
     */
    boolean save(DepositEntity depositEntity);
}
