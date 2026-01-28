package com.cognizant.smartpay.controller;

import com.cognizant.smartpay.dto.FingerprintAuthRequest;

import com.cognizant.smartpay.entity.User;

import com.cognizant.smartpay.entity.Wallet;

import com.cognizant.smartpay.exception.AuthenticationFailedException;

import com.cognizant.smartpay.exception.BiometricNotFoundException;

import com.cognizant.smartpay.service.AddWalletService;
import com.cognizant.smartpay.service.BiometricService;

import com.cognizant.smartpay.service.WalletService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

@RestController

@RequestMapping("/api/wallet")

@RequiredArgsConstructor

@Slf4j

@CrossOrigin(origins = "http://localhost:8081")

public class WalletController {

    private final BiometricService biometricService;

    private final AddWalletService addwalletService;

    /**

     * Get all active WebAuthn credentials

     */

    @GetMapping("/credentials")

    public ResponseEntity<?> getActiveCredentials() {

        try {

            List<String> credentials = biometricService.getAllActiveCredentials();

            Map<String, Object> response = new HashMap<>();

            response.put("success", true);

            response.put("credentials", credentials);

            response.put("count", credentials.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error("Error retrieving credentials", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(createErrorResponse("An error occurred: " + e.getMessage()));

        }

    }

    /**

     * Biometric authentication - returns user with wallet balance

     */

    @PostMapping("/authenticate")

    public ResponseEntity<?> authenticate(@RequestBody FingerprintAuthRequest request) {

        try {

            log.info("Biometric authentication request received");

            User user = biometricService.authenticateFingerprint(request);

            if (user == null) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)

                        .body(createErrorResponse("Authentication failed"));

            }

            // Get or create wallet for user

            Wallet wallet = addwalletService.getOrCreateWallet(user.getUserId());

            Map<String, Object> response = new HashMap<>();

            response.put("success", true);

            response.put("message", "Authentication successful");

            response.put("userId", user.getUserId());

            response.put("userName", user.getName());

            response.put("email", user.getEmail());

            response.put("biometricEnabled", user.getBiometricEnabled());

            response.put("walletBalance", wallet.getBalance());

            response.put("currency", wallet.getCurrency());

            log.info("User authenticated: {} with balance: {}", user.getEmail(), wallet.getBalance());

            return ResponseEntity.ok(response);

        } catch (AuthenticationFailedException e) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)

                    .body(createErrorResponse(e.getMessage()));

        } catch (BiometricNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)

                    .body(createErrorResponse(e.getMessage()));

        } catch (Exception e) {

            log.error("Error during authentication", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(createErrorResponse("An error occurred: " + e.getMessage()));

        }

    }

    /**

     * Backward compatibility

     */

    @PostMapping("/biometric-auth")

    public ResponseEntity<?> biometricAuth(@RequestBody FingerprintAuthRequest request) {

        return authenticate(request);

    }

    /**

     * Get wallet balance

     */

    @GetMapping("/{userId}/balance")

    public ResponseEntity<?> getBalance(@PathVariable Long userId) {

        try {

            log.info("Getting balance for userId: {}", userId);

            BigDecimal balance = addwalletService.getBalance(userId);

            Map<String, Object> response = new HashMap<>();

            response.put("balance", balance);

            response.put("currency", "INR");

            log.info("Balance for userId {}: {}", userId, balance);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error("Error getting balance", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(createErrorResponse(e.getMessage()));

        }

    }

    /**

     * Get wallet entity

     */

    @GetMapping("/{userId}")

    public ResponseEntity<?> getWallet(@PathVariable Long userId) {

        try {

            Wallet wallet = addwalletService.getOrCreateWallet(userId);

            Map<String, Object> response = new HashMap<>();

            response.put("walletId", wallet.getWalletId());

            response.put("userId", wallet.getUserId());

            response.put("balance", wallet.getBalance());

            response.put("currency", wallet.getCurrency());

            response.put("createdAt", wallet.getCreatedAt());

            response.put("updatedAt", wallet.getUpdatedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error("Error getting wallet", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(createErrorResponse(e.getMessage()));

        }

    }

    /**

     * Fund wallet - POST /api/wallet/{userId}/fund

     */

    @PostMapping("/{userId}/fund")

    public ResponseEntity<?> fundWallet(@PathVariable Long userId, @RequestBody Map<String, Object> request) {

        try {

            log.info("Fund wallet request for userId: {}", userId);

            Object amountObj = request.get("amount");

            if (amountObj == null) {

                return ResponseEntity.badRequest()

                        .body(createErrorResponse("Amount is required"));

            }

            BigDecimal amount = new BigDecimal(amountObj.toString());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                return ResponseEntity.badRequest()

                        .body(createErrorResponse("Amount must be greater than 0"));

            }

            // Use WalletService to add money

            Wallet wallet = addwalletService.addMoney(userId, amount);

            Map<String, Object> response = new HashMap<>();

            response.put("success", true);

            response.put("message", "Wallet funded successfully");

            response.put("balance", wallet.getBalance());

            response.put("newBalance", wallet.getBalance());

            response.put("currency", wallet.getCurrency());

            response.put("userId", userId);

            log.info("Wallet funded successfully. New balance: {}", wallet.getBalance());

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {

            return ResponseEntity.badRequest()

                    .body(createErrorResponse("Invalid amount format"));

        } catch (Exception e) {

            log.error("Error funding wallet", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(createErrorResponse("An error occurred: " + e.getMessage()));

        }

    }

    /**

     * Add money - POST /api/wallet/{userId}/add-money

     */

    @PostMapping("/{userId}/add-money")

    public ResponseEntity<?> addMoney(@PathVariable Long userId, @RequestBody Map<String, Object> request) {

        return fundWallet(userId, request);

    }

    /**

     * Debit wallet - POST /api/wallet/{userId}/debit

     */

    @PostMapping("/{userId}/debit")

    public ResponseEntity<?> debitWallet(@PathVariable Long userId, @RequestBody Map<String, Object> request) {

        try {

            log.info("Debit wallet request for userId: {}", userId);

            Object amountObj = request.get("amount");

            if (amountObj == null) {

                return ResponseEntity.badRequest()

                        .body(createErrorResponse("Amount is required"));

            }

            BigDecimal amount = new BigDecimal(amountObj.toString());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                return ResponseEntity.badRequest()

                        .body(createErrorResponse("Amount must be greater than 0"));

            }

            // Use WalletService to deduct money

            Wallet wallet = addwalletService.deductMoney(userId, amount);

            Map<String, Object> response = new HashMap<>();

            response.put("success", true);

            response.put("message", "Wallet debited successfully");

            response.put("balance", wallet.getBalance());

            response.put("newBalance", wallet.getBalance());

            response.put("currency", wallet.getCurrency());

            log.info("Wallet debited. New balance: {}", wallet.getBalance());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            if (e.getMessage().contains("Insufficient")) {

                return ResponseEntity.badRequest()

                        .body(createErrorResponse(e.getMessage()));

            }

            throw e;

        } catch (Exception e) {

            log.error("Error debiting wallet", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body(createErrorResponse("An error occurred: " + e.getMessage()));

        }

    }

    private Map<String, Object> createErrorResponse(String message) {

        Map<String, Object> error = new HashMap<>();

        error.put("success", false);

        error.put("error", message);

        return error;

    }

}
 