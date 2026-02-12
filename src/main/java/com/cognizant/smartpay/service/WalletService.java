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

    // ✅ NEW: Always ensure wallet exists with 0 balance
    @Transactional
    public Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            Wallet w = new Wallet();
            w.setUserId(userId);
            w.setBalance(BigDecimal.ZERO);
            w.setCurrency("INR");
            return walletRepository.save(w);
        });
    }

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId).orElse(null);
    }

    public BigDecimal getBalance(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
    }

    @Transactional
    public Wallet addMoney(Long userId, BigDecimal amount) {

        log.info("Adding ₹{} to wallet for userId: {}", amount, userId);

        Wallet wallet = getOrCreateWallet(userId);

        BigDecimal balanceBefore = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        wallet.setBalance(balanceAfter);
        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(savedWallet);
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

    public List<WalletTransaction> getTransactionHistory(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return walletTransactionRepository.findByWalletWalletIdOrderByCreatedAtDesc(wallet.getWalletId());
    }
}
