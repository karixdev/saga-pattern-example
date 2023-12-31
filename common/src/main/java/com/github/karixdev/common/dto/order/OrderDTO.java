package com.github.karixdev.common.dto.order;

import java.util.UUID;

public record OrderDTO(
        UUID id,
        UUID itemId,
        UUID userId,
        OrderStatus status
) {}
