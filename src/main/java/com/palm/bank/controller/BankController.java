package com.palm.bank.controller;

import com.palm.bank.common.ApiCode;
import com.palm.bank.common.ApiResult;
import com.palm.bank.config.BankConfig;
import com.palm.bank.dto.AccountDto;
import com.palm.bank.dto.CreateAccountDto;
import com.palm.bank.dto.LoginDto;
import com.palm.bank.dto.TransactionDto;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.entity.AccountTokenEntity;
import com.palm.bank.param.CreateAccountParam;
import com.palm.bank.param.LoginParam;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.AssetService;
import com.palm.bank.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.CipherException;
import org.web3j.utils.Convert;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Create new account in the database with new wallet address.
     * @param param CreateAcountParam structure
     *              name: new user name
     *              password: new user password
     * @return
     * @throws CipherException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws IOException
     */
    @PostMapping("/create-account")
    public ApiResult<CreateAccountDto> createAccount(@Validated @RequestBody CreateAccountParam param) throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        log.info("create account: name={}, password={}", param.getName(), param.getPassword());

        AccountEntity accountEntity = accountService.findByName(param.getName());
        if (accountEntity != null) {
            return ApiResult.result(ApiCode.ALREADY_EXIST_ACCOUNT, CreateAccountDto.builder().address(accountEntity.getAddress()).build());
        }

        try {
            accountEntity = assetService.createNewWallet(param.getName(), param.getPassword());
            return ApiResult.result(ApiCode.SUCCESS, CreateAccountDto.builder().address(accountEntity.getAddress()).build());
        } catch (Exception ex) {
            return ApiResult.internalError();
        }
    }

    /**
     * Login account with user name and password, and return auth token.
     * If not exist user, return the code of NOT_FOUND_ACCOUNT.
     * @param loginParam LoginParam structure
     *                   name: user name to login
     *                   password: user password to login
     * @return new auth token
     */
    @PostMapping("/login")
    public ApiResult<LoginDto> login(@Validated @RequestBody LoginParam loginParam) {
        log.info("login: name={}, password={}", loginParam.getName(), loginParam.getPassword());

        AccountTokenEntity accountTokenEntity = loginService.login(loginParam.getName(), loginParam.getPassword());
        if (accountTokenEntity == null) {
            return ApiResult.result(ApiCode.NOT_FOUND_ACCOUNT, null);
        }
        return ApiResult.result(ApiCode.SUCCESS, LoginDto.builder().token(accountTokenEntity.getToken()).build());
    }

    /**
     * Get all the registered accounts with its wallet address and balance
     * @return
     */
    @GetMapping("/accounts")
    public ApiResult<List<AccountDto>> getAccounts() {
        log.info("accounts");

        return ApiResult.result(ApiCode.SUCCESS, accountService.findAll().stream().map(accountEntity -> AccountDto.builder().name(accountEntity.getName()).address(accountEntity.getAddress()).balance(accountEntity.getBalance().toString()).build()).collect(Collectors.toList()));
    }

    /**
     * Get the balance of user in blockchains
     * @param address
     * @return
     * @throws IOException
     */
    @GetMapping("/balance/{address}")
    public ApiResult<String> getBalance(@PathVariable String address) throws IOException {
        BigDecimal balance = assetService.getBalance(address);
        return ApiResult.result(ApiCode.SUCCESS, Convert.fromWei(balance, Convert.Unit.ETHER).toString());
    }

    /**
     * Get the internal balance in bank, should be logged prior to call
     * @param request
     * @return
     * @throws IOException
     */
    @GetMapping("/internal-balance")
    public ApiResult<String> getInternalBalance(HttpServletRequest request) throws IOException {
        String accountToken = request.getHeader("Token");
        AccountTokenEntity accountTokenEntity = loginService.isValid(accountToken);
        if (accountTokenEntity == null) {
            return ApiResult.result(ApiCode.INVALID_TOKEN, null);
        }

        AccountEntity accountEntity = accountService.findById(accountTokenEntity.getAccountId());
        if (accountEntity == null) {
            return ApiResult.result(ApiCode.NOT_FOUND_ACCOUNT, null);
        }
        return ApiResult.result(ApiCode.SUCCESS, Convert.fromWei(accountEntity.getBalance(), Convert.Unit.ETHER).toString());
    }

    /**
     * Transfer tokens in bank, should be logged in prior to call
     * @param to target wallet address which is already registered in bank
     * @param amount transfer amount
     * @param request
     * @return transaction hash
     */
    @GetMapping("/transfer")
    public ApiResult<TransactionDto> transfer(String to, BigDecimal amount, HttpServletRequest request) {
        String accountToken = request.getHeader("Token");
        AccountTokenEntity accountTokenEntity = loginService.isValid(accountToken);
        if (accountTokenEntity == null) {
            return ApiResult.result(ApiCode.INVALID_TOKEN, null);
        }

        AccountEntity fromAccount = accountService.findById(accountTokenEntity.getAccountId());
        if (fromAccount == null) {
            return ApiResult.result(ApiCode.NOT_FOUND_ACCOUNT, null);
        }
        try {
            AccountEntity toAccount = accountService.findByAddress(to);
            if (toAccount == null) {
                return ApiResult.result(ApiCode.NOT_REGISTERED_ACCOUNT, null);
            }

            log.info("balance: {} > {}", fromAccount.getBalance().toString(), amount.toString());
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                return ApiResult.result(ApiCode.NOT_ENOUGH_BALANCE, null);
            }

            BigDecimal fee = amount.multiply(new BigDecimal(bankConfig.getFeeInPercent())).divide(BigDecimal.TEN.pow(4));
            amount = amount.subtract(fee);
            log.info("internal-transfer: to={}, amount={}, fee={}", to, amount.toString(), fee.toString());

            // Move balance within database
            String txHash = assetService.internalTransfer(fromAccount, toAccount, amount, fee);
            return ApiResult.result(ApiCode.SUCCESS, TransactionDto.builder().txHash(txHash).build());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ApiResult.internalError();
        }
    }

    /**
     * Deposit tokens, means transfer tokens from blockchain to bank, the balance of current account
     * will be increased after confirmation.
     * @param amount token amount to deposit
     * @param request
     * @return
     */
    @GetMapping("/deposit")
    public ApiResult<TransactionDto> deposit(BigDecimal amount, HttpServletRequest request) {
        String accountToken = request.getHeader("Token");
        AccountTokenEntity accountTokenEntity = loginService.isValid(accountToken);
        if (accountTokenEntity == null) {
            return ApiResult.result(ApiCode.INVALID_TOKEN, null);
        }

        AccountEntity accountEntity = accountService.findById(accountTokenEntity.getAccountId());
        if (accountEntity == null) {
            return ApiResult.result(ApiCode.NOT_FOUND_ACCOUNT, null);
        }

        try {
            if (assetService.getBalance(accountEntity.getAddress()).compareTo(amount) < 0) {
                return ApiResult.result(ApiCode.NOT_ENOUGH_BALANCE, null);
            }
            log.info("deposit: from={}, amount={}", accountEntity.getAddress(), amount.toString());

            // Transfer tokens from Withdraw wallet to current account on blockchains
            String txHash = assetService.deposit(accountEntity, amount);
            return ApiResult.result(ApiCode.SUCCESS, TransactionDto.builder().txHash(txHash).build());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ApiResult.internalError();
        }
    }

    /**
     * Withdraw tokens, means transfer tokens from bank to blockchain, the balance of current account
     * will be decreased and transferred tokens.
     * @param amount token amount to withdraw
     * @param request
     * @return
     */
    @GetMapping("/withdraw")
    public ApiResult<TransactionDto> withdraw(BigDecimal amount, HttpServletRequest request) {
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

        try {
            BigDecimal fee = amount.multiply(new BigDecimal(bankConfig.getFeeInPercent())).divide(BigDecimal.TEN.pow(4));
            amount = amount.subtract(fee);
            log.info("withdraw: to={}, amount={}, fee={}, request={}", accountEntity.getAddress(), amount.toString(), fee.toString());

            // Transfer tokens from Withdraw wallet to current account on blockchains
            String txHash = assetService.withdraw(accountEntity, amount, fee);
            return ApiResult.result(ApiCode.SUCCESS, TransactionDto.builder().txHash(txHash).build());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ApiResult.internalError();
        }
    }
}
