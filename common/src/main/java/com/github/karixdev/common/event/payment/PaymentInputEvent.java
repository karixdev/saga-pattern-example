package com.github.karixdev.common.event.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentInputEvent(
    PaymentInputEventType type,
    UUID orderId,
    UUID userId,
    BigDecimal amount
) {}
