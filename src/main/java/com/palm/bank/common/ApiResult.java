package com.palm.bank.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@Getter
@AllArgsConstructor
public class ApiResult<T> implements Serializable {

    private ApiCode code;

    private T data;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.FFF")
    private Date time;

    public static <T> ApiResult<T> result(ApiCode code, T data) {
        return (ApiResult<T>) ApiResult.builder().code(code).data(data).time(new Date()).build();
    }

    public static ApiResult internalError() {
        return ApiResult.result(ApiCode.FAILED, "Internal Server Error");
    }
}
