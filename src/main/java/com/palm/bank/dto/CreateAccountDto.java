package com.palm.bank.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateAccountDto {

    /**
     * Wallet address on blockchain
     */
    String address;
}
