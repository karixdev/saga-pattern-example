package com.github.karixdev.paymentservice.repository;

import com.github.karixdev.paymentservice.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    @Query("""
            SELECT bankAccount
            FROM BankAccount bankAccount
            WHERE bankAccount.userId = :userId
            """)
    Optional<BankAccount> findByUserId(UUID userId);

    @Query("""
            SELECT bankAccount
            FROM BankAccount bankAccount
            LEFT JOIN FETCH bankAccount.payments
            """)
    List<BankAccount> findAllWithPayments();

}
