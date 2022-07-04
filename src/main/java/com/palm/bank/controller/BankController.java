package com.palm.bank.controller;

import com.palm.bank.common.ApiCode;
import com.palm.bank.common.ApiResult;
import com.palm.bank.config.BankConfig;
import com.palm.bank.dto.AccountDto;
import com.palm.bank.dto.CreateAccountDto;
import com.palm.bank.dto.TransactionDto;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.param.CreateAccountParam;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.AssetService;
import com.palm.bank.service.TransactionService;
import com.palm.bank.util.JwtTokenUtil;
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

    private final BankConfig bankConfig;

    private final AccountService accountService;

    private final AssetService assetService;

    private final TransactionService transactionService;

    public BankController(BankConfig bankConfig, AccountService accountService, AssetService assetService, TransactionService transactionService) {
        this.bankConfig = bankConfig;
        this.accountService = accountService;
        this.assetService = assetService;
        this.transactionService = transactionService;
    }

    /**
     * Create new account in the database with new wallet address.
     *
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
    public ApiResult<CreateAccountDto> createAccount(@Validated @RequestBody CreateAccountParam param)
            throws CipherException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        log.info("create account: name={}, password={}", param.getName(), param.getPassword());

        try {
            AccountEntity accountEntity = accountService.findByName(param.getName());
            if (accountEntity != null) {
                return ApiResult.result(ApiCode.ALREADY_EXIST_ACCOUNT, CreateAccountDto.builder().address(accountEntity.getAddress()).build());
            }

            accountEntity = assetService.createNewWallet(param.getName(), param.getPassword());
            return ApiResult.result(ApiCode.SUCCESS, CreateAccountDto.builder().address(accountEntity.getAddress()).build());
        } catch (Exception ex) {
            log.error(ex.toString());
            return ApiResult.internalError();
        }
    }

    /**
     * Get all the registered accounts with its wallet address and balance
     *
     * @return
     */
    @GetMapping("/accounts")
    public ApiResult<List<AccountDto>> getAccounts() {
        log.info("accounts");

        try {
            return ApiResult.result(
                    ApiCode.SUCCESS,
                    accountService.findAll()
                            .stream().map(
                            accountEntity ->
                                    AccountDto.builder()
                                            .name(accountEntity.getName())
                                            .address(accountEntity.getAddress())
                                            .balance(Convert.fromWei(accountEntity.getBalance(), Convert.Unit.ETHER).toString())
                                            .build()
                    ).collect(Collectors.toList()));
        } catch (Exception ex) {
            log.error(ex.toString());
            return ApiResult.internalError();
        }
    }

    /**
     * Get the balance of user in blockchains in Ether unit
     *
     * @param address
     * @return
     * @throws IOException
     */
    @GetMapping("/balance/{address}")
    public ApiResult<String> getBalance(@PathVariable String address) throws IOException {
        try {
            BigDecimal balance = assetService.getBalance(address);
            // Convert balance to Ether unit
            return ApiResult.result(ApiCode.SUCCESS, Convert.fromWei(balance, Convert.Unit.ETHER).toString());
        } catch (Exception ex) {
            log.error(ex.toString());
            return ApiResult.internalError();
        }
    }

    /**
     * Get the internal balance in bank, should be logged prior to call
     *
     * @param request
     * @return
     * @throws IOException
     */
    @GetMapping("/internal-balance")
    public ApiResult<String> getInternalBalance(HttpServletRequest request) throws IOException {
        try {
            String username = JwtTokenUtil.getUsername(request);
            AccountEntity accountEntity = accountService.findByName(username);
            if (accountEntity == null) {
                return ApiResult.result(ApiCode.NOT_FOUND_ACCOUNT, null);
            }

            // Convert balance to Ether unit
            return ApiResult.result(ApiCode.SUCCESS, Convert.fromWei(accountEntity.getBalance(), Convert.Unit.ETHER).toString());
        } catch (Exception ex) {
            log.error(ex.toString());
            return ApiResult.internalError();
        }
    }

    @GetMapping("/transactions")
    public ApiResult<List<TransactionDto>> getTransactions(HttpServletRequest request) throws IOException {
        try {
            String username = JwtTokenUtil.getUsername(request);
            AccountEntity accountEntity = accountService.findByName(username);
            if (accountEntity == null) {
                return ApiResult.result(ApiCode.NOT_FOUND_ACCOUNT, null);
            }

            return ApiResult.result(ApiCode.SUCCESS, transactionService.findAll().stream().map(transactionEntity ->
                    TransactionDto.builder()
                            .txHash(transactionEntity.getTxHash())
                            .from(transactionEntity.getFromAddress())
                            .to(transactionEntity.getToAddress())
                            .amount(transactionEntity.getAmount())
                            .build()
            ).collect(Collectors.toList()));
        } catch (Exception ex) {
            log.error(ex.toString());
            return ApiResult.internalError();
        }

    }

    /**
     * Transfer tokens in bank, should be logged in prior to call
     *
     * @param to      target wallet address which is already registered in bank
     * @param amount  transfer amount in Ether unit
     * @param request
     * @return transaction hash
     */
    @GetMapping("/transfer")
    public ApiResult<TransactionDto> transfer(String to, BigDecimal amount, HttpServletRequest request) {
        try {
            String username = JwtTokenUtil.getUsername(request);
            AccountEntity fromAccount = accountService.findByName(username);
            if (fromAccount == null) {
                return ApiResult.result(ApiCode.NOT_FOUND_ACCOUNT, null);
            }
            AccountEntity toAccount = accountService.findByAddress(to);
            if (toAccount == null) {
                return ApiResult.result(ApiCode.NOT_REGISTERED_ACCOUNT, null);
            }

            // Convert amount parameter to Wei unit
            amount = Convert.toWei(amount, Convert.Unit.ETHER);

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
            log.error(ex.toString());
            return ApiResult.internalError();
        }
    }

    /**
     * Deposit tokens, means transfer tokens from blockchain to bank, the balance of current account
     * will be increased after confirmation.
     *
     * @param amount  token amount to deposit in Ether unit
     * @param request
     * @return
     */
    @GetMapping("/deposit")
    public ApiResult<TransactionDto> deposit(BigDecimal amount, HttpServletRequest request) {
        try {
            String username = JwtTokenUtil.getUsername(request);
            AccountEntity accountEntity = accountService.findByName(username);
            if (accountEntity == null) {
                return ApiResult.result(ApiCode.NOT_FOUND_ACCOUNT, null);
            }

            // Convert amount parameter to Wei unit
            amount = Convert.toWei(amount, Convert.Unit.ETHER);

            if (assetService.getBalance(accountEntity.getAddress()).compareTo(amount) < 0) {
                return ApiResult.result(ApiCode.NOT_ENOUGH_BALANCE, null);
            }
            log.info("deposit: from={}, amount={}", accountEntity.getAddress(), amount.toString());

            // Transfer tokens from Withdraw wallet to current account on blockchains
            String txHash = assetService.deposit(accountEntity, amount);
            return ApiResult.result(ApiCode.SUCCESS, TransactionDto.builder().txHash(txHash).build());
        } catch (Exception ex) {
            log.error(ex.toString());
            return ApiResult.internalError();
        }
    }

    /**
     * Withdraw tokens, means transfer tokens from bank to blockchain, the balance of current account
     * will be decreased and transferred tokens.
     *
     * @param amount  token amount to withdraw in Ether unit
     * @param request
     * @return
     */
    @GetMapping("/withdraw")
    public ApiResult<TransactionDto> withdraw(BigDecimal amount, HttpServletRequest request) {
        try {
            String username = JwtTokenUtil.getUsername(request);
            AccountEntity accountEntity = accountService.findByName(username);
            if (accountEntity == null) {
                return ApiResult.internalError();
            }
            if (accountEntity.getBalance().compareTo(amount) < 0) {
                return ApiResult.result(ApiCode.NOT_ENOUGH_BALANCE, null);
            }

            // Convert amount parameter to Wei unit
            amount = Convert.toWei(amount, Convert.Unit.ETHER);

            BigDecimal fee = amount.multiply(new BigDecimal(bankConfig.getFeeInPercent())).divide(BigDecimal.TEN.pow(4));
            amount = amount.subtract(fee);
            log.info("withdraw: to={}, amount={}, fee={}, request={}", accountEntity.getAddress(), amount.toString(), fee.toString());

            // Transfer tokens from Withdraw wallet to current account on blockchains
            String txHash = assetService.withdraw(accountEntity, amount, fee);
            return ApiResult.result(ApiCode.SUCCESS, TransactionDto.builder().txHash(txHash).build());
        } catch (Exception ex) {
            log.error(ex.toString());
            return ApiResult.internalError();
        }
    }
}
