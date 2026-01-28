package com.cognizant.smartpay.entity;

import jakarta.persistence.*;

import lombok.Data;

import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;

import java.time.LocalDateTime;

@Entity

@Table(name = "wallet_transactions")

@Data

@NoArgsConstructor

@AllArgsConstructor

public class WalletTransaction {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "wallet_transaction_id")

    private Long walletTransactionId;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "wallet_id", nullable = false)

    private Wallet wallet;

    @Enumerated(EnumType.STRING)

    @Column(name = "transaction_type", nullable = false)

    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)

    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 10, scale = 2)

    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 10, scale = 2)

    private BigDecimal balanceAfter;

    @Column(name = "reference_id", length = 50)

    private String referenceId;

    @Enumerated(EnumType.STRING)

    @Column(name = "reference_type")

    private ReferenceType referenceType;

    @Column(name = "description", columnDefinition = "TEXT")

    private String description;

    @Column(name = "created_at")

    private LocalDateTime createdAt;

    @PrePersist

    protected void onCreate() {

        createdAt = LocalDateTime.now();

    }

    public enum TransactionType {

        CREDIT, DEBIT, REFUND

    }

    public enum ReferenceType {

        PURCHASE, TOPUP, REFUND, ADJUSTMENT

    }

}
