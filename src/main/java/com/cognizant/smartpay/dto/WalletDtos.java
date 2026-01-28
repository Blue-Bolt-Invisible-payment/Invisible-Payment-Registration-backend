package com.cognizant.smartpay.dto;

import lombok.Data;

import lombok.Builder;

import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;

public class WalletDtos {

    @Data

    public static class BalanceResponse {

        private BigDecimal balance;

        private String currency;

        public BalanceResponse() {}

        public BalanceResponse(BigDecimal balance, String currency) {

            this.balance = balance;

            this.currency = currency;

        }

    }

    @Data

    public static class FundRequest {

        private BigDecimal amount;

        private String referenceId;

        private String referenceType;  // TOPUP, REFUND

        private String description;

    }

    @Data

    public static class FundResponse {

        private boolean success;

        private BigDecimal balance;

        private BigDecimal newBalance;

        private String currency;

        private String message;

        private String transactionId;

    }

    @Data

    @Builder

    @NoArgsConstructor

    @AllArgsConstructor

    public static class BiometricAuthResponse {

        private boolean success;

        private String message;

        private Long userId;

        private UserInfo user;

    }

    @Data

    @Builder

    @NoArgsConstructor

    @AllArgsConstructor

    public static class UserInfo {

        private Long id;

        private String name;

        private String email;

        private BigDecimal walletBalance;

        private boolean biometricEnabled;

        private String status;

    }

}
 