package com.github.karixdev.common.event.payment;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record PaymentInputEvent(
    PaymentInputEventType type,
    UUID orderId,
    UUID userId,
    BigDecimal amount
) {}
