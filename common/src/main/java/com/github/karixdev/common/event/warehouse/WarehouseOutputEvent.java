package com.github.karixdev.common.event.warehouse;

import com.github.karixdev.common.dto.warehouse.ItemDTO;
import lombok.Builder;

import java.util.UUID;

@Builder
public record WarehouseOutputEvent(
    WarehouseOutputEventType type,
    UUID orderId,
    ItemDTO itemDTO
) {}
