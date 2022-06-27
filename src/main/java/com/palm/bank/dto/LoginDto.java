package com.palm.bank.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginDto {

    /**
     * Auth token after login
     */
    private String token;
}
