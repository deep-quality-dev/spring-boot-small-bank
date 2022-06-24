package com.palm.bank.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginParam {

    @NotBlank(message = "Not empty name")
    private String name;

    private String password;
}
