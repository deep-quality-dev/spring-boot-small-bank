package com.palm.bank.common;

import lombok.Getter;

@Getter
public enum ApiCode {

    SUCCESS(0, "SUCCESS"),

    FAILED(1, "FAILED"),

    ALREADY_EXIST_ACCOUNT(100, "ALREADY EXIST ACCOUNT"),

    NOT_FOUND_ACCOUNT(101, "NOT FOUND ACCOUNT"),

    INVALID_TOKEN(102, "INVALID TOKEN"),

    NOT_REGISTERED_ACCOUNT(103, "NOT REGISTERED ACCOUNT"),

    NOT_ENOUGH_BALANCE(104, "NOT ENOUGH BALANCE"),

    FAILED_TX(105, "FAFILED IN TRANSACTION");

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
