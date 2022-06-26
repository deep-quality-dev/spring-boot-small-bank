package com.palm.bank.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "deposits")
public class DepositEntity {

    @Id
    @Column(nullable = false, length = 64)
    private String txHash;

    @Column(nullable = false, length = 32)
    private String blockHash;

    @Column(nullable = false)
    private Long blockNumber;

    @Column(nullable = false)
    private Date time;

    @Column(nullable = false)
    private String amount;

    @Column(nullable = false, length = 32)
    private String address;

    @Column(nullable = false)
    private int status;

    public BigDecimal getAmount() {
        return new BigDecimal(amount);
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount.toString();
    }
}
