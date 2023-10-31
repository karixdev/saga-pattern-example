package com.github.karixdev.paymentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(
            name = "order_id",
            nullable = false
    )
    private UUID orderId;

    @Column(
            name = "amount",
            nullable = false
    )
    private BigDecimal amount;

}
