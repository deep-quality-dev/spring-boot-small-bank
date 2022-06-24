package com.palm.bank.repository;

import com.palm.bank.entity.AccountTokenEntity;
import com.palm.bank.entity.BalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("balanceRepository")
public interface BalanceRepository extends JpaRepository<BalanceEntity, String> {

    @Query(value = "SELECT * FROM balances b WHERE b.address = :address", nativeQuery = true)
    AccountTokenEntity findByAddress(@Param("address") Long address);
}
