package com.cognizant.smartpay.service;

import com.cognizant.smartpay.entity.Wallet;

import com.cognizant.smartpay.entity.WalletTransaction;

import com.cognizant.smartpay.entity.WalletTransaction.ReferenceType;

import com.cognizant.smartpay.entity.WalletTransaction.TransactionType;

import com.cognizant.smartpay.repository.WalletRepository;

import com.cognizant.smartpay.repository.WalletTransactionRepository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;

@Service

@Slf4j

@RequiredArgsConstructor

public class WalletService {

    private final WalletRepository walletRepository;

    private final WalletTransactionRepository walletTransactionRepository;

    /**

     * Get wallet by user ID

     */

    public Wallet getWalletByUserId(Long userId) {

        return walletRepository.findByUserId(userId).orElse(null);

    }

    /**

     * Get balance for a user

     */

    public BigDecimal getBalance(Long userId) {

        Wallet wallet = getWalletByUserId(userId);

        return wallet != null ? wallet.getBalance() : BigDecimal.ZERO;

    }

    /**

     * Add money to wallet - saves to DB and creates transaction record

     */

    @Transactional

    public Wallet addMoney(Long userId, BigDecimal amount) {

        log.info("Adding ₹{} to wallet for userId: {}", amount, userId);

        Wallet wallet = walletRepository.findByUserId(userId)

                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));

        BigDecimal balanceBefore = wallet.getBalance();

        BigDecimal balanceAfter = balanceBefore.add(amount);

        // Update wallet balance

        wallet.setBalance(balanceAfter);

        Wallet savedWallet = walletRepository.save(wallet);

        // Create transaction record for audit

        WalletTransaction transaction = new WalletTransaction();

        transaction.setWallet(wallet);

        transaction.setTransactionType(TransactionType.CREDIT);

        transaction.setAmount(amount);

        transaction.setBalanceBefore(balanceBefore);

        transaction.setBalanceAfter(balanceAfter);

        transaction.setReferenceType(ReferenceType.TOPUP);

        transaction.setDescription("Wallet top-up");

        walletTransactionRepository.save(transaction);

        log.info("SUCCESS: Wallet updated. Before: ₹{}, Added: ₹{}, After: ₹{}",

                balanceBefore, amount, balanceAfter);

        return savedWallet;

    }

    /**

     * Debit money from wallet

     */

    @Transactional

    public Wallet debitMoney(Long userId, BigDecimal amount, String description) {

        log.info("Debiting ₹{} from wallet for userId: {}", amount, userId);

        Wallet wallet = walletRepository.findByUserId(userId)

                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));

        BigDecimal balanceBefore = wallet.getBalance();

        if (balanceBefore.compareTo(amount) < 0) {

            throw new RuntimeException("Insufficient balance. Available: ₹" + balanceBefore);

        }

        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        wallet.setBalance(balanceAfter);

        Wallet savedWallet = walletRepository.save(wallet);

        // Create transaction record

        WalletTransaction transaction = new WalletTransaction();

        transaction.setWallet(wallet);

        transaction.setTransactionType(TransactionType.DEBIT);

        transaction.setAmount(amount);

        transaction.setBalanceBefore(balanceBefore);

        transaction.setBalanceAfter(balanceAfter);

        transaction.setReferenceType(ReferenceType.PURCHASE);

        transaction.setDescription(description != null ? description : "Purchase");

        walletTransactionRepository.save(transaction);

        log.info("SUCCESS: Wallet debited. Before: ₹{}, Debited: ₹{}, After: ₹{}",

                balanceBefore, amount, balanceAfter);

        return savedWallet;

    }

    /**

     * Get transaction history

     */

    public List<WalletTransaction> getTransactionHistory(Long userId) {

        Wallet wallet = getWalletByUserId(userId);

        if (wallet == null) return List.of();

        return walletTransactionRepository.findByWalletWalletIdOrderByCreatedAtDesc(wallet.getWalletId());

    }

}
 