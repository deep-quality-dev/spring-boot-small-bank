package com.palm.bank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palm.bank.entity.AccountEntity;
import com.palm.bank.service.AccountService;
import com.palm.bank.service.AssetService;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
public class BankControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AssetService assetService;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private String name = "name", password = "password", address = "address";
    private AccountEntity accountEntity;

    private String authorization;

    @BeforeAll
    public void setUp() {
        accountEntity = AccountEntity.builder()
                .name(name)
                .encodedPassword(bCryptPasswordEncoder.encode(password))
                .filename("filename")
                .balance("0")
                .address(address)
                .build();
    }

    @Test
    @Order(1)
    public void createAccount() throws Exception {
        doReturn(true)
                .when(accountService)
                .save(any(AccountEntity.class));
        doReturn(null)
                .when(accountService)
                .findByName(any(String.class));

        doReturn(accountEntity)
                .when(assetService)
                .createNewWallet(name, password);

        HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("password", password);

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

    @Test
    @Order(2)
    public void login() throws Exception {
        when(accountService.loadUserByUsername(name)).thenReturn(
                new User(accountEntity.getName(), accountEntity.getEncodedPassword(),
                        true, true, true, true, new ArrayList<>()));

        HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("password", password);

        MvcResult mvcResult = this.mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(map)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andReturn();

        authorization = mvcResult.getResponse().getHeader("Authorization");
    }

    @Test
    @Order(3)
    public void internalBalance() throws Exception {
        Assert.assertNotNull(authorization);
        when(accountService.findByName(name)).thenReturn(accountEntity);

        this.mockMvc.perform(get("/v1/bank/internal-balance")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        this.mockMvc.perform(get("/v1/bank/internal-balance")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authorization))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value("0"));
    }
}
