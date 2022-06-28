package com.palm.bank.repository;

import com.palm.bank.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository("transactionRepository")
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    @Query(value = "SELECT * FROM transactions d WHERE d.tx_hash = :txHash", nativeQuery = true)
    TransactionEntity findByHash(String txHash);
}
