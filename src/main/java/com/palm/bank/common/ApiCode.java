package com.palm.bank.common;

import lombok.Getter;

@Getter
public enum ApiCode {

    SUCCESS(0, "SUCCESS"),

    FAILED(1, "FAILED"),

    ALREADY_EXIST_ACCOUNT(100, "ALREADY EXIST ACCOUNT"),

    NOT_FOUND_ACCOUNT(101, "NOT FOUND ACCOUNT");

    private final int code;
    private final String message;

    ApiCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    public static ApiCode getApiCode(int code) {
       ApiCode[] codes = ApiCode.values();

       for (ApiCode ec : codes) {
           if (ec.getCode() == code) {
               return ec;
           }
       }

       return SUCCESS;
    }
}
