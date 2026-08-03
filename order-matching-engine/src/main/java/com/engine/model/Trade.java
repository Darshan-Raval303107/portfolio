package com.engine.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a completed trade or order fill in the engine.
 *
 * @param tradeId      Unique identifier for the trade execution.
 * @param buyOrderId   The order ID of the buying side of the trade.
 * @param sellOrderId  The order ID of the selling side of the trade.
 * @param price        The price at which the trade was executed.
 * @param quantity     The quantity filled in this trade execution.
 * @param timestamp    The timestamp of when the execution occurred.
 */
public record Trade(
        String tradeId,
        String buyOrderId,
        String sellOrderId,
        BigDecimal price,
        BigDecimal quantity,
        Instant timestamp
) {
    public Trade {
        Objects.requireNonNull(tradeId, "tradeId cannot be null");
        Objects.requireNonNull(buyOrderId, "buyOrderId cannot be null");
        Objects.requireNonNull(sellOrderId, "sellOrderId cannot be null");
        Objects.requireNonNull(price, "price cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Trade price must be positive");
        }
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Trade quantity must be positive");
        }
    }
}
