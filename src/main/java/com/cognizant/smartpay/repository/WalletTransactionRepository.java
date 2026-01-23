package com.cognizant.smartpay.repository;
import com.cognizant.smartpay.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletWalletIdOrderByCreatedAtDesc(Long walletId);
    List<WalletTransaction> findTop10ByWalletWalletIdOrderByCreatedAtDesc(Long walletId);
}