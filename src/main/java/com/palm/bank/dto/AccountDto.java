package com.palm.bank.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountDto {

    /**
     * Account name in bank
     */
    private String name;

    /**
     * Wallet address on blockchain
     */
    private String address;

    /**
     * Balance in bank
     */
    private String balance;
}
