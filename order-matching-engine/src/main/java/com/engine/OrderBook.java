package com.engine;

import com.engine.model.Order;
import com.engine.model.OrderType;
import com.engine.model.Side;
import com.engine.model.Trade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages the order book for a specific symbol.
 * Maintains bid and ask sides with price-time priority.
 */
public class OrderBook {
    private final String symbol;
    private final TreeSet<Order> bids;
    private final TreeSet<Order> asks;
    private final AtomicLong tradeIdGenerator;

    /**
     * Constructs an OrderBook for the specified symbol.
     *
     * @param symbol the trading symbol (e.g. "BTCUSD").
     */
    public OrderBook(String symbol) {
        this.symbol = Objects.requireNonNull(symbol, "symbol cannot be null");
        this.tradeIdGenerator = new AtomicLong(0);

        // Bids: Sorted by price descending, then timestamp ascending, then orderId ascending.
        this.bids = new TreeSet<>((o1, o2) -> {
            int priceCompare = o2.getPrice().compareTo(o1.getPrice());
            if (priceCompare != 0) {
                return priceCompare;
            }
            int timeCompare = o1.getTimestamp().compareTo(o2.getTimestamp());
            if (timeCompare != 0) {
                return timeCompare;
            }
            return o1.getOrderId().compareTo(o2.getOrderId());
        });

        // Asks: Sorted by price ascending, then timestamp ascending, then orderId ascending.
        this.asks = new TreeSet<>((o1, o2) -> {
            int priceCompare = o1.getPrice().compareTo(o2.getPrice());
            if (priceCompare != 0) {
                return priceCompare;
            }
            int timeCompare = o1.getTimestamp().compareTo(o2.getTimestamp());
            if (timeCompare != 0) {
                return timeCompare;
            }
            return o1.getOrderId().compareTo(o2.getOrderId());
        });
    }

    /**
     * Processes an incoming order.
     * Matches the order against resting orders on the opposite side of the book.
     * Any unfilled limit order quantity is added to the order book.
     * Any unfilled market order quantity is cancelled/discarded.
     *
     * @param incomingOrder the order to process.
     * @return a list of trades executed as a result of this order.
     */
    public synchronized List<Trade> process(Order incomingOrder) {
        Objects.requireNonNull(incomingOrder, "incomingOrder cannot be null");
        if (!incomingOrder.getSymbol().equals(symbol)) {
            throw new IllegalArgumentException("Order symbol " + incomingOrder.getSymbol() + " does not match book symbol " + symbol);
        }

        List<Trade> executedTrades = new ArrayList<>();

        if (incomingOrder.getSide() == Side.BUY) {
            matchOrder(incomingOrder, asks, executedTrades);
            if (!incomingOrder.isFilled() && incomingOrder.getType() == OrderType.LIMIT) {
                bids.add(incomingOrder);
            }
        } else {
            matchOrder(incomingOrder, bids, executedTrades);
            if (!incomingOrder.isFilled() && incomingOrder.getType() == OrderType.LIMIT) {
                asks.add(incomingOrder);
            }
        }

        return executedTrades;
    }

    private void matchOrder(Order incomingOrder, TreeSet<Order> oppositeBook, List<Trade> executedTrades) {
        while (!incomingOrder.isFilled() && !oppositeBook.isEmpty()) {
            Order bestOpposite = oppositeBook.first();

            // If incoming is limit order, verify the prices cross
            if (incomingOrder.getType() == OrderType.LIMIT) {
                if (incomingOrder.getSide() == Side.BUY) {
                    if (incomingOrder.getPrice().compareTo(bestOpposite.getPrice()) < 0) {
                        break; // No cross
                    }
                } else {
                    if (incomingOrder.getPrice().compareTo(bestOpposite.getPrice()) > 0) {
                        break; // No cross
                    }
                }
            }

            // Calculate fill quantity
            BigDecimal fillQty = incomingOrder.getRemainingQuantity().min(bestOpposite.getRemainingQuantity());
            
            // Execute trade
            incomingOrder.fill(fillQty);
            bestOpposite.fill(fillQty);

            String tradeId = "T-" + tradeIdGenerator.incrementAndGet();
            String buyOrderId = incomingOrder.getSide() == Side.BUY ? incomingOrder.getOrderId() : bestOpposite.getOrderId();
            String sellOrderId = incomingOrder.getSide() == Side.SELL ? incomingOrder.getOrderId() : bestOpposite.getOrderId();
            
            // Execution price is the resting order's price
            BigDecimal tradePrice = bestOpposite.getPrice();

            Trade trade = new Trade(tradeId, buyOrderId, sellOrderId, tradePrice, fillQty, Instant.now());
            executedTrades.add(trade);

            if (bestOpposite.isFilled()) {
                oppositeBook.remove(bestOpposite);
            }
        }
    }

    /**
     * Returns an unmodifiable view of resting buy orders.
     *
     * @return unmodifiable set of bids.
     */
    public synchronized Set<Order> getBids() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(bids));
    }

    /**
     * Returns an unmodifiable view of resting sell orders.
     *
     * @return unmodifiable set of asks.
     */
    public synchronized Set<Order> getAsks() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(asks));
    }

    /**
     * Clears all orders from the book.
     */
    public synchronized void clear() {
        bids.clear();
        asks.clear();
    }
}
