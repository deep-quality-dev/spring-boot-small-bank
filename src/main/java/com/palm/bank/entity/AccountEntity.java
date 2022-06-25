package com.palm.bank.entity;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String name;

    @Column(nullable = true, length = 32)
    private String password;
    
    @Column(nullable = false, length = 256)
    private String filename;

    @Column(nullable = false, length = 64, unique = true)
    private String address; // wallet address on blockchain

    @Column(nullable = true)
    private BigDecimal balance;
}
