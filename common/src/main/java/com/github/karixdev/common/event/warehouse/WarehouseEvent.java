package com.github.karixdev.common.event.warehouse;

import com.github.karixdev.common.dto.order.OrderDTO;
import com.github.karixdev.common.dto.warehouse.ItemDTO;

import java.util.UUID;

public record WarehouseEvent(
        WarehouseEventType type,
        ItemDTO itemDTO,
        OrderDTO order
) {
    public WarehouseEvent(WarehouseEventType type, OrderDTO order) {
        this(type, new ItemDTO(order.itemId(), null), order);
    }
}
