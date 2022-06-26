package com.palm.bank.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "deposits")
public class DepositEntity {

    @Id
    @Column(nullable = false, length = 128)
    private String txHash;

    @Column(nullable = false, length = 128)
    private String blockHash;

    @Column(nullable = false)
    private Long blockNumber;

    @Column(nullable = false, length = 64)
    private String amount;

    @Column(nullable = false, length = 64)
    private String address;
}
