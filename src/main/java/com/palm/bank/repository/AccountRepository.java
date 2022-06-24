package com.palm.bank.repository;

import com.palm.bank.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("accountRepository")
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    @Query(value = "SELECT * FROM accounts a WHERE a.name = :name", nativeQuery = true)
    AccountEntity findByName(@Param("name") String name);
}
