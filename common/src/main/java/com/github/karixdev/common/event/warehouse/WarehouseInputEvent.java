package com.github.karixdev.common.event.warehouse;

import com.github.karixdev.common.dto.order.OrderDTO;
import com.github.karixdev.common.dto.warehouse.ItemDTO;

import java.util.UUID;

public record WarehouseInputEvent(
        WarehouseEventInputType type,
        UUID orderId,
        UUID itemId
) {}
