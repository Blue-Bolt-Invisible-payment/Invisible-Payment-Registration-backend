package com.cognizant.smartpay.controller;

import com.cognizant.smartpay.dto.FingerprintAuthRequest;
import com.cognizant.smartpay.entity.User;
import com.cognizant.smartpay.entity.Wallet;
import com.cognizant.smartpay.entity.WalletTransaction;
import com.cognizant.smartpay.exception.AuthenticationFailedException;
import com.cognizant.smartpay.exception.BiometricNotFoundException;
import com.cognizant.smartpay.repository.UserRepository;
import com.cognizant.smartpay.repository.WalletTransactionRepository;
//import com.cognizant.smartpay.service.AddWalletService;
import com.cognizant.smartpay.service.AddWalletService;
import com.cognizant.smartpay.service.BiometricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WalletController {

    private final BiometricService biometricService;
    private final AddWalletService addwalletService;
    private final WalletTransactionRepository walletTransactionRepository;

    // ✅ NEW: to get user name for /balance response
    private final UserRepository userRepository;

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

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody FingerprintAuthRequest request) {
        try {
            log.info("Biometric authentication request received");
            User user = biometricService.authenticateFingerprint(request);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Authentication failed"));
            }

            Wallet wallet = addwalletService.getOrCreateWallet(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Authentication successful");
            response.put("userId", user.getUserId());
            response.put("userName", user.getName());
            response.put("email", user.getEmail());
            response.put("biometricEnabled", user.getBiometricEnabled());
            response.put("walletBalance", wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance());
            response.put("currency", wallet.getCurrency());

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

    @PostMapping("/biometric-auth")
    public ResponseEntity<?> biometricAuth(@RequestBody FingerprintAuthRequest request) {
        return authenticate(request);
    }

    /**
     * ✅ UPDATED: Get wallet balance
     * Returns keys HomeScreen expects: wallet_balance + name
     */
    @GetMapping("/{userId}/balance")
    public ResponseEntity<?> getBalance(@PathVariable Long userId) {
        try {
            log.info("Getting balance for userId: {}", userId);

            // ✅ ensure wallet exists with balance default 0
            Wallet wallet = addwalletService.getOrCreateWallet(userId);
            BigDecimal bal = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();

            // ✅ fetch user name
            String name = userRepository.findById(userId)
                    .map(User::getName)
                    .orElse("User");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user_id", userId);
            response.put("name", name);

            // ✅ IMPORTANT: HomeScreen expects wallet_balance
            response.put("wallet_balance", bal);

            response.put("currency", wallet.getCurrency() == null ? "INR" : wallet.getCurrency());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting balance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getWallet(@PathVariable Long userId) {
        try {
            Wallet wallet = addwalletService.getOrCreateWallet(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("walletId", wallet.getWalletId());
            response.put("userId", wallet.getUserId());
            response.put("balance", wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance());
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

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<?> getRecentTransactions(@PathVariable Long userId) {
        try {
            log.info("Fetching transactions for userId: {}", userId);

            Optional<Wallet> wallet = addwalletService.findByUserId(userId);
            if (wallet.isPresent()) {
                List<WalletTransaction> transactions =
                        walletTransactionRepository.findTop10ByWalletWalletIdOrderByCreatedAtDesc(wallet.get().getWalletId());
                return ResponseEntity.ok(transactions);
            }

            // ✅ if no wallet exists, create and return empty list
            addwalletService.getOrCreateWallet(userId);
            return ResponseEntity.ok(Collections.emptyList());

        } catch (Exception e) {
            log.error("Error fetching transactions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{userId}/fund")
    public ResponseEntity<?> fundWallet(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        try {
            Object amountObj = request.get("amount");
            if (amountObj == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Amount is required"));
            }

            BigDecimal amount = new BigDecimal(amountObj.toString());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(createErrorResponse("Amount must be greater than 0"));
            }

            Wallet wallet = addwalletService.addMoney(userId, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Wallet funded successfully");
            response.put("userId", userId);

            // ✅ return wallet_balance also (helps UI if needed)
            response.put("wallet_balance", wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance());
            response.put("currency", wallet.getCurrency());

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Invalid amount format"));
        } catch (Exception e) {
            log.error("Error funding wallet", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/{userId}/add-money")
    public ResponseEntity<?> addMoney(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        return fundWallet(userId, request);
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return error;
    }
}
