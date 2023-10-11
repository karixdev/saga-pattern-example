package com.github.karixdev.common.dto.warehouse;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemDTO(
        UUID itemId,
        BigDecimal price
) {}
