package com.cognizant.smartpay.service;

import com.cognizant.smartpay.entity.Wallet;

import com.cognizant.smartpay.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.Optional;

@Service

@Slf4j

@RequiredArgsConstructor

public class AddWalletService {

    private final WalletRepository walletRepository;

    /**

     * Create wallet for a user

     */

    @Transactional

    public Wallet createWallet(Long userId, BigDecimal initialBalance) {

        log.info("Creating wallet for userId: {} with initial balance: {}", userId, initialBalance);

        // Check if wallet already exists

        Optional<Wallet> existingWallet = walletRepository.findByUserId(userId);

        if (existingWallet.isPresent()) {

            log.info("Wallet already exists for userId: {}", userId);

            return existingWallet.get();

        }

        Wallet wallet = new Wallet();

        wallet.setUserId(userId);

        wallet.setBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);

        wallet.setCurrency("INR");

        wallet.setCreatedAt(LocalDateTime.now());

        wallet.setUpdatedAt(LocalDateTime.now());

        wallet = walletRepository.save(wallet);

        log.info("Wallet created successfully with ID: {}", wallet.getWalletId());

        return wallet;

    }

    /**

     * Get wallet by userId

     */

    public Optional<Wallet> getWalletByUserId(Long userId) {

        return walletRepository.findByUserId(userId);

    }

    /**

     * Get or create wallet for user

     */

    @Transactional

    public Wallet getOrCreateWallet(Long userId) {

        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);

        if (walletOpt.isPresent()) {

            return walletOpt.get();

        }

        return createWallet(userId, BigDecimal.ZERO);

    }

    /**

     * Add money to wallet

     */

    @Transactional

    public Wallet addMoney(Long userId, BigDecimal amount) {

        log.info("Adding {} to wallet for userId: {}", amount, userId);

        Wallet wallet = getOrCreateWallet(userId);

        BigDecimal newBalance = wallet.getBalance().add(amount);

        wallet.setBalance(newBalance);

        wallet.setUpdatedAt(LocalDateTime.now());

        wallet = walletRepository.save(wallet);

        log.info("Money added. New balance: {}", newBalance);

        return wallet;

    }

    /**

     * Deduct money from wallet

     */

    @Transactional

    public Wallet deductMoney(Long userId, BigDecimal amount) {

        log.info("Deducting {} from wallet for userId: {}", amount, userId);

        Wallet wallet = getOrCreateWallet(userId);

        if (wallet.getBalance().compareTo(amount) < 0) {

            throw new RuntimeException("Insufficient balance");

        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);

        wallet.setBalance(newBalance);

        wallet.setUpdatedAt(LocalDateTime.now());

        wallet = walletRepository.save(wallet);

        log.info("Money deducted. New balance: {}", newBalance);

        return wallet;

    }

    /**

     * Get balance

     */

    public BigDecimal getBalance(Long userId) {

        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);

        if (walletOpt.isPresent()) {

            return walletOpt.get().getBalance();

        }

        return BigDecimal.ZERO;

    }
    /**
     * Find wallet by User ID
     */
    public Optional<Wallet> findByUserId(Long userId) {
        // Assuming your repository has findByUserId or similar
        return walletRepository.findByUserId(userId);
    }

}
