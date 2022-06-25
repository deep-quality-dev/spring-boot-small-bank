package com.palm.bank.controller;

import com.palm.bank.common.ApiCode;
import com.palm.bank.common.ApiResult;
import com.palm.bank.common.Unit;
import com.palm.bank.config.BankConfig;
import com.palm.bank.dto.CreateAccountDto;
import com.palm.bank.dto.LoginDto;
import com.palm.bank.dto.TransferDto;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.AccountTokenEntity;
import com.palm.bank.param.CreateAccountParam;
import com.palm.bank.param.LoginParam;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.AssetService;
import com.palm.bank.service.LoginService;
import com.palm.bank.util.EtherConvert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.CipherException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

@Slf4j
@RestController
@RequestMapping("/v1/bank")
public class BankController {

    @Autowired
    private final BankConfig bankConfig;

    @Autowired
    private final LoginService loginService;

    @Autowired
    private final AccountService accountService;

    @Autowired
    private final AssetService assetService;

    public BankController(BankConfig bankConfig, LoginService loginService, AccountService accountService, AssetService assetService) {
        this.bankConfig = bankConfig;
        this.loginService = loginService;
        this.accountService = accountService;
        this.assetService = assetService;
    }

    @PostMapping("/create-account")
    public ApiResult<CreateAccountDto> createAccount(@Validated @RequestBody CreateAccountParam param) throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        log.info("create account: name={}, password={}", param.getName(), param.getPassword());

        AccountEntity accountEntity = accountService.findByName(param.getName());
        if (accountEntity != null) {
            return ApiResult.result(ApiCode.ALREADY_EXIST_ACCOUNT, CreateAccountDto.builder().address(accountEntity.getAddress()).build());
        }
        
        try {
            accountEntity = accountService.saveOne(param.getName(), param.getPassword());
            return ApiResult.result(ApiCode.SUCCESS, CreateAccountDto.builder().address(accountEntity.getAddress()).build());
        } catch (Exception ex) {
            return ApiResult.internalError();
        }
    }

    @PostMapping("/login")
    public ApiResult<LoginDto> login(@Validated @RequestBody LoginParam loginParam) {
        log.info("login: name={}, password={}", loginParam.getName(), loginParam.getPassword());

        AccountTokenEntity accountTokenEntity = loginService.login(loginParam.getName(), loginParam.getPassword());
        if (accountTokenEntity == null) {
            return ApiResult.result(ApiCode.NOT_FOUND_ACCOUNT, null);
        }
        return ApiResult.result(ApiCode.SUCCESS, LoginDto.builder().token(accountTokenEntity.getToken()).build());
    }

    @GetMapping("/balance/{address}")
    public ApiResult<BigDecimal> getBalance(@PathVariable String address) throws IOException {
        BigDecimal balance = assetService.getBalance(address);
        return ApiResult.result(ApiCode.SUCCESS, EtherConvert.fromWei(balance, Unit.ETHER));
    }

    @GetMapping("/internal-balance/{address}")
    public ApiResult<BigDecimal> getInternalBalance(@PathVariable String address, HttpServletRequest request) throws IOException {
        String accountToken = request.getHeader("Token");
        AccountTokenEntity accountTokenEntity = loginService.isValid(accountToken);
        if (accountTokenEntity == null) {
            return ApiResult.result(ApiCode.INVALID_TOKEN, null);
        }

        AccountEntity accountEntity = accountService.findById(accountTokenEntity.getAccountId());
        if (accountEntity == null) {
            return ApiResult.internalError();
        }
        return ApiResult.result(ApiCode.SUCCESS, EtherConvert.fromWei(accountEntity.getBalance(), Unit.ETHER));
    }

    @GetMapping("/transfer")
    public ApiResult<TransferDto> transfer(String to, BigDecimal amount, HttpServletRequest request) {
        String accountToken = request.getHeader("Token");
        AccountTokenEntity accountTokenEntity = loginService.isValid(accountToken);
        if (accountTokenEntity == null) {
            return ApiResult.result(ApiCode.INVALID_TOKEN, null);
        }

        AccountEntity fromAccount = accountService.findById(accountTokenEntity.getAccountId());
        if (fromAccount == null) {
            return ApiResult.internalError();
        }
        AccountEntity toAccount = accountService.findByAddress(to);
        if (fromAccount == null) {
            return ApiResult.result(ApiCode.NOT_REGISTERED_ACCOUNT, null);
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            return ApiResult.result(ApiCode.NOT_ENOUGH_BALANCE, null);
        }

        BigDecimal fee = amount.multiply(new BigDecimal(bankConfig.getFeeInPercent())).divide(BigDecimal.TEN.pow(4));
        log.info("internal-transfer: to={}, amount={}, fee={}, request={}", to, amount.toString(), fee.toString());

        // Move balance within database
        String txHash = assetService.transfer(fromAccount, toAccount, amount, fee);
        accountService.update(fromAccount);
        return ApiResult.result(ApiCode.SUCCESS, TransferDto.builder().txHash(txHash).build());
    }

    @GetMapping("/withdraw")
    public ApiResult<TransferDto> withdraw(BigDecimal amount, HttpServletRequest request) {
        String accountToken = request.getHeader("Token");
        AccountTokenEntity accountTokenEntity = loginService.isValid(accountToken);
        if (accountTokenEntity == null) {
            return ApiResult.result(ApiCode.INVALID_TOKEN, null);
        }

        AccountEntity accountEntity = accountService.findById(accountTokenEntity.getAccountId());
        if (accountEntity == null) {
            return ApiResult.internalError();
        }

        if (accountEntity.getBalance().compareTo(amount) < 0) {
            return ApiResult.result(ApiCode.NOT_ENOUGH_BALANCE, null);
        }

        BigDecimal fee = amount.multiply(new BigDecimal(bankConfig.getFeeInPercent())).divide(BigDecimal.TEN.pow(4));
        log.info("withdraw: to={}, amount={}, fee={}, request={}", accountEntity.getAddress(), amount.toString(), fee.toString());

        // Transfer tokens from Withdraw wallet to current account on blockchains
        String txHash = assetService.withdraw(accountEntity, amount, fee);
        return ApiResult.result(ApiCode.SUCCESS, TransferDto.builder().txHash(txHash).build());
    }
}
