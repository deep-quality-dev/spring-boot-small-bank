package com.palm.bank.repository;

import com.palm.bank.entity.AccountTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("accountTokenRepository")
public interface AccountTokenRepository extends JpaRepository<AccountTokenEntity, Long> {

    @Query(value = "SELECT * FROM account_tokens at WHERE at.accountId = :accountId", nativeQuery = true)
    AccountTokenEntity findByAccountId(@Param("accountId") Long accountId);
}
