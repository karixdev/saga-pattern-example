package com.github.karixdev.common.event.warehouse;

import com.github.karixdev.common.dto.warehouse.ItemDTO;

import java.util.UUID;

public record WarehouseOutputEvent(
    WarehouseOutputEventType type,
    UUID orderId,
    ItemDTO itemDTO
) {}
