package com.github.karixdev.common.event.warehouse;

import lombok.Builder;

import java.util.UUID;

@Builder
public record WarehouseInputEvent(
        WarehouseEventInputType type,
        UUID orderId,
        UUID itemId
) {}
