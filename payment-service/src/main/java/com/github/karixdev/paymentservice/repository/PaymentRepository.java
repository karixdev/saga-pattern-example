package com.github.karixdev.paymentservice.repository;

import com.github.karixdev.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("""
            SELECT payment
            FROM Payment payment
            WHERE payment.orderId = :orderId
            """)
    Optional<Payment> findByOrderId(UUID orderId);

}
