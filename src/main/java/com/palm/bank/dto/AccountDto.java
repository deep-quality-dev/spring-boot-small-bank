package com.palm.bank.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountDto {

    private String name;

    private String address;

    private String balance;
}
