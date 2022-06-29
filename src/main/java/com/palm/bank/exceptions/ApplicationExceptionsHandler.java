package com.palm.bank.exceptions;

import com.palm.bank.common.ApiResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ApplicationExceptionsHandler {

    /**
     * Returns Internal Server Error if exception occurs
     *
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ApiResult> handleServiceException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ApiResult.internalError(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
