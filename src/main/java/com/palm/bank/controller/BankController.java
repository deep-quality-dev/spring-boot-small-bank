package com.palm.bank.controller;

import com.palm.bank.common.ApiCode;
import com.palm.bank.common.ApiResult;
import com.palm.bank.common.Unit;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.AccountTokenEntity;
import com.palm.bank.param.CreateAccountParam;
import com.palm.bank.param.LoginParam;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.EtherService;
import com.palm.bank.service.LoginService;
import com.palm.bank.util.EtherConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/v1/bank")
public class BankController {

    @Autowired
    private final LoginService loginService;

    @Autowired
    private final AccountService accountService;

    @Autowired
    private final EtherService etherService;

    public BankController(LoginService loginService, AccountService accountService, EtherService etherService) {
        this.loginService = loginService;
        this.accountService = accountService;
        this.etherService = etherService;
    }

    @PostMapping("/create-account")
    public ApiResult<String> createAccount(@Validated @RequestBody CreateAccountParam param) {
        log.info("create account: {}, {}", param.getName(), param.getPassword());

        AccountEntity accountEntity = accountService.findByName(param.getName());
        if (accountEntity != null) {
            return ApiResult.result(ApiCode.ALREADY_EXIST_ACCOUNT, "Already exists");
        }

        accountEntity = accountService.create(param.getName(), param.getPassword());
        if (accountEntity == null) {
            return ApiResult.internalError();
        }
        return ApiResult.result(ApiCode.SUCCESS, accountEntity.getAddress());
    }

    @PostMapping("/login")
    public ApiResult<Boolean> login(@Validated @RequestBody LoginParam loginParam) {
        log.info("login: {}, {}", loginParam.getName(), loginParam.getPassword());

        AccountTokenEntity accountTokenEntity = loginService.login(loginParam.getName(), loginParam.getPassword());
        if (accountTokenEntity == null) {
            return ApiResult.result(ApiCode.NOT_FOUND_ACCOUNT, false);
        }
        return ApiResult.result(ApiCode.SUCCESS, true);
    }

    @GetMapping("/balance/{address}")
    public ApiResult<BigDecimal> getBalance(@PathVariable String address) throws IOException {
        BigDecimal balance = etherService.getBalance(address);

        return ApiResult.result(ApiCode.SUCCESS, EtherConvert.fromWei(balance, Unit.ETHER));
    }
}
