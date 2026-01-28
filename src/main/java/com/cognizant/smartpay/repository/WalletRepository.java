package com.cognizant.smartpay.repository;

import com.cognizant.smartpay.entity.Wallet;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.Optional;

/**

 * Repository for Wallet entity operations

 */

@Repository

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**

     * Find wallet by user ID

     */

    Optional<Wallet> findByUserId(Long userId);

    /**

     * Check if wallet exists for user

     */

    boolean existsByUserId(Long userId);

    /**

     * Find wallet by user ID using custom query (alternative method)

     */

    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")

    Optional<Wallet> findWalletByUserId(@Param("userId") Long userId);

}
