package com.cognizant.smartpay.repository;

import com.cognizant.smartpay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by email and enabled status
     */
    Optional<User> findByEmailAndEnabled(String email, Boolean enabled);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find user by phone number
     */
    Optional<User> findByPhone(String phone);

    /**
     * Check if phone number exists
     */
    boolean existsByPhone(String phone);

    /**
     * Find user by email specifically for authentication verification
     */
    Optional<User> findByEmailAndStatus(String email, String status);
// Newly added for login part...

    Optional<User> findByEmailIgnoreCaseAndPassword(String email, String password);

    Optional<User> findByPhoneAndPassword(String phone, String password);

    default Optional<User> findByIdentifierAndPassword(String identifier, String password) {
        boolean looksLikePhone = identifier.matches("^\\+?\\d{10,15}$");
        if (looksLikePhone) {
            return findByPhoneAndPassword(identifier, password);
        }
        return findByEmailIgnoreCaseAndPassword(identifier, password);
    }
}