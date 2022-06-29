package com.palm.bank.security;

import com.palm.bank.config.BankConfig;
import com.palm.bank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Autowired
    private final BankConfig bankConfig;

    @Autowired
    private final AccountService userDetailsService;

    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SecurityConfiguration(BankConfig bankConfig, AccountService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bankConfig = bankConfig;
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder);
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        return httpSecurity
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                // Permit signup url to allow to call without authentication
                .antMatchers(HttpMethod.POST, SecurityConstants.SIGNUP_URL)
                .permitAll()
                // permit /accounts, /balance, and H2 console url to allow to call without authentication
                .antMatchers(
                        SecurityConstants.BANK_URL + "/accounts",
                        SecurityConstants.BANK_URL + "/balance/**",
                        SecurityConstants.H2_CONSOLE)
                .permitAll()
                .anyRequest().authenticated()
                .and()
                // Use customized authenticationManager which has BCryptPasswordEncoder as passwordEncoder and userDetailsService
                .authenticationManager(authenticationManager)
                // Customized authentication filter with new login url
                .addFilter(getAuthenticationFilter(authenticationManager))
                // Customized authorization filter
                .addFilter(new AuthorizationFilter(authenticationManager))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    public AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) {
        final AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager);
        authenticationFilter.setFilterProcessesUrl("/users/login");
        return authenticationFilter;
    }
}
