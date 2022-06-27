package com.palm.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palm.bank.dto.AccountDto;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.AssetService;
import com.palm.bank.service.LoginService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BankControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginService loginService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AssetService assetService;

    @Test
    public void createAccount() throws Exception {
        doReturn(true)
                .when(accountService)
                .save(any(AccountEntity.class));
        doReturn(null)
                .when(accountService)
                .findByName(any(String.class));

        String name = "name", password = "password", address = "address", balance = "0";

        AccountEntity accountEntity = AccountEntity.builder()
                .name(name)
                .password(password)
                .filename("filename")
                .balance(balance)
                .address(address)
                .build();
        doReturn(accountEntity)
                .when(assetService)
                .createNewWallet(name, password);

        HashMap<String, String> map = new HashMap<>();
        map.put("name", "name");
        map.put("password", "password");

        MvcResult mvcResult = this.mockMvc.perform(post("/v1/bank/create-account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.address").value(address))
                .andReturn();

        Assert.assertEquals("application/json", mvcResult.getResponse().getContentType());
    }
}
