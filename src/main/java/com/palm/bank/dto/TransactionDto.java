package com.palm.bank.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionDto {

    /**
     * Transaction hash on blockchain
     */
    private String txHash;
}
