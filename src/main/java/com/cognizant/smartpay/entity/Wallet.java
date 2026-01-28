package com.cognizant.smartpay.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;

import lombok.Data;

import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import java.time.LocalDateTime;

@Entity

@Table(name = "wallet")

@Data

@NoArgsConstructor

@AllArgsConstructor

public class Wallet {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "wallet_id")

    private Long walletId;

    @Column(name = "user_id", nullable = false, unique = true)

    private Long userId;

    @Column(precision = 15, scale = 2)

    private BigDecimal balance = BigDecimal.ZERO;

    @Column(length = 3)

    private String currency = "INR";

    @Column(name = "created_at")

    private LocalDateTime createdAt;

    @Column(name = "updated_at")

    private LocalDateTime updatedAt;

    @PrePersist

    protected void onCreate() {

        createdAt = LocalDateTime.now();

        updatedAt = LocalDateTime.now();

        if (balance == null) {

            balance = BigDecimal.ZERO;

        }

        if (currency == null) {

            currency = "INR";

        }

    }

    @PreUpdate

    protected void onUpdate() {

        updatedAt = LocalDateTime.now();

    }

}
