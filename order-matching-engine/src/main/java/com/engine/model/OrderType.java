package com.engine.model;

/**
 * Represents the type of order.
 */
public enum OrderType {
    /**
     * A limit order executes at the specified price or better.
     */
    LIMIT,

    /**
     * A market order executes immediately at the best available market prices.
     */
    MARKET
}
