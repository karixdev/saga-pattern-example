package com.github.karixdev.common.event.payment;

import java.util.UUID;

public record PaymentOutputEvent(
   PaymentOutputEventType type,
   UUID orderId
) {}
