package com.engine.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents an order in the matching engine.
 * Supports both LIMIT and MARKET order types.
 */
public class Order {
    private final String orderId;
    private final String symbol;
    private final Side side;
    private final OrderType type;
    private final BigDecimal price; // null for MARKET orders
    private final BigDecimal quantity;
    private BigDecimal remainingQuantity;
    private final Instant timestamp;

    /**
     * Constructs a new Order.
     *
     * @param orderId           Unique identifier of the order.
     * @param symbol            The trading instrument symbol.
     * @param side              The trading side (BUY or SELL).
     * @param type              The order type (LIMIT or MARKET).
     * @param price             The order price (must be null for MARKET, non-null and positive for LIMIT).
     * @param quantity          The quantity of the order (must be positive).
     * @param timestamp         The order creation/entry timestamp.
     */
    public Order(String orderId, String symbol, Side side, OrderType type, BigDecimal price, BigDecimal quantity, Instant timestamp) {
        this.orderId = Objects.requireNonNull(orderId, "orderId cannot be null");
        this.symbol = Objects.requireNonNull(symbol, "symbol cannot be null");
        this.side = Objects.requireNonNull(side, "side cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");

        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
        this.remainingQuantity = quantity;

        if (type == OrderType.MARKET) {
            if (price != null) {
                throw new IllegalArgumentException("Market order cannot have a price");
            }
            this.price = null;
        } else {
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Limit order must have a positive price");
            }
            this.price = price;
        }
    }

    public String getOrderId() {
        return orderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public Side getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if the order is fully filled.
     *
     * @return true if remaining quantity is zero, false otherwise.
     */
    public boolean isFilled() {
        return remainingQuantity.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Reduces the remaining quantity of the order by the specified filled amount.
     *
     * @param filledQuantity The quantity to subtract from remaining.
     */
    public void fill(BigDecimal filledQuantity) {
        if (filledQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Filled quantity must be greater than zero");
        }
        if (filledQuantity.compareTo(remainingQuantity) > 0) {
            throw new IllegalArgumentException("Filled quantity cannot exceed remaining quantity");
        }
        this.remainingQuantity = this.remainingQuantity.subtract(filledQuantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId.equals(order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", side=" + side +
                ", type=" + type +
                ", price=" + price +
                ", quantity=" + quantity +
                ", remainingQuantity=" + remainingQuantity +
                ", timestamp=" + timestamp +
                '}';
    }
}
