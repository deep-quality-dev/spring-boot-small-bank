package com.palm.bank.service.impl;

import com.palm.bank.entity.TransactionEntity;
import com.palm.bank.repository.TransactionRepository;
import com.palm.bank.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("transactionService")
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public boolean exists(TransactionEntity transactionEntity) {
        TransactionEntity found = transactionRepository.findByHash(transactionEntity.getTxHash());
        return found != null ? true : false;
    }

    @Override
    public boolean save(TransactionEntity transactionEntity) {
        transactionRepository.save(transactionEntity);
        return true;
    }

    @Override
    public List<TransactionEntity> findAll() {
        return transactionRepository.findAll();
    }
}
