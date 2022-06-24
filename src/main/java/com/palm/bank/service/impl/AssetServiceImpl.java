package com.palm.bank.service.impl;

import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.AccountTokenEntity;
import com.palm.bank.entity.AssetEntity;
import com.palm.bank.repository.AssetRepository;
import com.palm.bank.service.AssetService;
import com.palm.bank.util.TokenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Service("assetService")
public class AssetServiceImpl implements AssetService {

    @Autowired
    private final AssetRepository assetRepository;

    public AssetServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public BigDecimal getBalance(String address) {
        AssetEntity assetEntity = this.assetRepository.findByAddress(address);
        return assetEntity == null ? null : assetEntity.getBalance();
    }
}
