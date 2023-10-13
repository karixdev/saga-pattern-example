package com.github.karixdev.common.event.warehouse;

import com.github.karixdev.common.dto.order.OrderDTO;
import com.github.karixdev.common.dto.warehouse.ItemDTO;

public record WarehouseInputEvent(
        WarehouseEventInputType type,
        OrderDTO order
) {}
