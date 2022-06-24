package com.palm.bank.repository;

import com.palm.bank.entity.AccountTokenEntity;
import com.palm.bank.entity.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository("assetRepository")
public interface AssetRepository extends JpaRepository<AssetEntity, String> {

    @Query(value = "SELECT * FROM assets a WHERE a.address = :address", nativeQuery = true)
    AssetEntity findByAddress(@Param("address") String address);
}
