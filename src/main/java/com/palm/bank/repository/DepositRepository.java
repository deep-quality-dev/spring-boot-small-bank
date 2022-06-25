package com.palm.bank.repository;

import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.DepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("depositRepository")
public interface DepositRepository extends JpaRepository<DepositEntity, Long> {

    @Query(value = "SELECT * FROM deposits d WHERE d.txHash = :txHash", nativeQuery = true)
    DepositEntity findByHash(String txHash);
}
