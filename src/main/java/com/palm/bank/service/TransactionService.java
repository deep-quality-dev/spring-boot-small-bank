package com.palm.bank.service;

import com.palm.bank.entity.TransactionEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TransactionService {

    /**
     * Check if deposit transaction was already registered in `deposits` table
     *
     * @param transactionEntity
     * @return
     */
    boolean exists(TransactionEntity transactionEntity);

    /**
     * Save deposit transaction in `deposits` table
     *
     * @param transactionEntity
     * @return
     */
    boolean save(TransactionEntity transactionEntity);

    List<TransactionEntity> findAll();
}
