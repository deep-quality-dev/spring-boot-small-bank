package com.palm.bank.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "balances")
public class BalanceEntity {

    @Id
    @Column(nullable = false, length = 32)
    private String address;

    @Column(nullable = true)
    private BigDecimal balance;
    
    @Column(nullable = false)
    private Date updated;
}
