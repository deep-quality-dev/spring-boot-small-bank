package com.palm.bank.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDto {

    /**
     * Transaction hash on blockchain
     */
    private String txHash;

    private String from;

    private String to;

    private String amount;
}
