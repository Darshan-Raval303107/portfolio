package com.engine;

import com.engine.model.Order;
import com.engine.model.OrderType;
import com.engine.model.Side;
import com.engine.model.Trade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {

    private OrderBook orderBook;
    private static final String SYMBOL = "AAPL";

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook(SYMBOL);
    }

    @Test
    void testEmptyOrderBook() {
        // GIVEN: An empty order book
        assertTrue(orderBook.getBids().isEmpty());
        assertTrue(orderBook.getAsks().isEmpty());

        // WHEN: A market order is submitted
        Order marketOrder = new Order("O1", SYMBOL, Side.BUY, OrderType.MARKET, null, new BigDecimal("100"), Instant.now());
        List<Trade> trades = orderBook.process(marketOrder);

        // THEN: No trades execute, and market order is not added to the book
        assertTrue(trades.isEmpty());
        assertTrue(orderBook.getBids().isEmpty());
        assertTrue(orderBook.getAsks().isEmpty());
    }

    @Test
    void testLimitOrderResting() {
        // GIVEN: An empty order book
        Order limitOrder = new Order("O1", SYMBOL, Side.BUY, OrderType.LIMIT, new BigDecimal("150.00"), new BigDecimal("10"), Instant.now());

        // WHEN: Processing a limit order that doesn't match anything
        List<Trade> trades = orderBook.process(limitOrder);

        // THEN: No trades execution, order becomes resting bid
        assertTrue(trades.isEmpty());
        assertEquals(1, orderBook.getBids().size());
        assertTrue(orderBook.getAsks().isEmpty());
        assertEquals(limitOrder, orderBook.getBids().iterator().next());
    }

    @Test
    void testPriceTimePriorityAndMultipleOrdersSamePrice() {
        Instant baseTime = Instant.now();

        // Add buy orders at different prices and times
        Order buy1 = new Order("O1", SYMBOL, Side.BUY, OrderType.LIMIT, new BigDecimal("100.00"), new BigDecimal("10"), baseTime);
        Order buy2 = new Order("O2", SYMBOL, Side.BUY, OrderType.LIMIT, new BigDecimal("101.00"), new BigDecimal("10"), baseTime.plusMillis(10));
        Order buy3 = new Order("O3", SYMBOL, Side.BUY, OrderType.LIMIT, new BigDecimal("100.00"), new BigDecimal("10"), baseTime.minusMillis(10)); // Earlier timestamp than O1

        orderBook.process(buy1);
        orderBook.process(buy2);
        orderBook.process(buy3);

        // Bids order expected:
        // 1. O2 (Price 101.00 - highest price)
        // 2. O3 (Price 100.00, earlier timestamp)
        // 3. O1 (Price 100.00, later timestamp)
        Set<Order> bids = orderBook.getBids();
        assertEquals(3, bids.size());

        Order[] bidsArr = bids.toArray(new Order[0]);
        assertEquals("O2", bidsArr[0].getOrderId());
        assertEquals("O3", bidsArr[1].getOrderId());
        assertEquals("O1", bidsArr[2].getOrderId());
    }

    @Test
    void testFullAndPartialFills() {
        Instant now = Instant.now();
        // Resting sell order: 10 units at 150.00
        Order sell1 = new Order("O1", SYMBOL, Side.SELL, OrderType.LIMIT, new BigDecimal("150.00"), new BigDecimal("10"), now);
        orderBook.process(sell1);

        // Incoming buy limit order: 15 units at 150.00
        Order buy1 = new Order("O2", SYMBOL, Side.BUY, OrderType.LIMIT, new BigDecimal("150.00"), new BigDecimal("15"), now.plusMillis(1));
        List<Trade> trades = orderBook.process(buy1);

        // Matches O1 fully (10 units)
        assertEquals(1, trades.size());
        Trade trade = trades.getFirst();
        assertEquals("O2", trade.buyOrderId());
        assertEquals("O1", trade.sellOrderId());
        assertEquals(new BigDecimal("150.00"), trade.price());
        assertEquals(new BigDecimal("10"), trade.quantity());

        // O1 should be removed from asks
        assertTrue(orderBook.getAsks().isEmpty());

        // O2 is partially filled (5 units remaining) and sits on bids book
        assertEquals(1, orderBook.getBids().size());
        Order remainingBid = orderBook.getBids().iterator().next();
        assertEquals("O2", remainingBid.getOrderId());
        assertEquals(new BigDecimal("5"), remainingBid.getRemainingQuantity());
    }

    @Test
    void testCrossedBook() {
        Instant now = Instant.now();
        // Resting buy order at 100.00
        Order buy1 = new Order("O1", SYMBOL, Side.BUY, OrderType.LIMIT, new BigDecimal("100.00"), new BigDecimal("10"), now);
        orderBook.process(buy1);

        // Incoming sell order crosses the book (price 95.00)
        Order sell1 = new Order("O2", SYMBOL, Side.SELL, OrderType.LIMIT, new BigDecimal("95.00"), new BigDecimal("10"), now.plusMillis(1));
        List<Trade> trades = orderBook.process(sell1);

        // It should match immediately at the resting price (100.00)
        assertEquals(1, trades.size());
        Trade trade = trades.getFirst();
        assertEquals("O1", trade.buyOrderId());
        assertEquals("O2", trade.sellOrderId());
        assertEquals(new BigDecimal("100.00"), trade.price());
        assertEquals(new BigDecimal("10"), trade.quantity());

        assertTrue(orderBook.getBids().isEmpty());
        assertTrue(orderBook.getAsks().isEmpty());
    }

    @Test
    void testMarketOrders() {
        Instant now = Instant.now();
        // Resting sell orders
        Order sell1 = new Order("O1", SYMBOL, Side.SELL, OrderType.LIMIT, new BigDecimal("100.00"), new BigDecimal("5"), now);
        Order sell2 = new Order("O2", SYMBOL, Side.SELL, OrderType.LIMIT, new BigDecimal("101.00"), new BigDecimal("10"), now.plusMillis(1));
        orderBook.process(sell1);
        orderBook.process(sell2);

        // Incoming buy market order for 8 units
        Order marketBuy = new Order("O3", SYMBOL, Side.BUY, OrderType.MARKET, null, new BigDecimal("8"), now.plusMillis(2));
        List<Trade> trades = orderBook.process(marketBuy);

        // Should generate 2 trades:
        // 1. 5 units at 100.00
        // 2. 3 units at 101.00
        assertEquals(2, trades.size());

        Trade trade1 = trades.get(0);
        assertEquals("O3", trade1.buyOrderId());
        assertEquals("O1", trade1.sellOrderId());
        assertEquals(new BigDecimal("100.00"), trade1.price());
        assertEquals(new BigDecimal("5"), trade1.quantity());

        Trade trade2 = trades.get(1);
        assertEquals("O3", trade2.buyOrderId());
        assertEquals("O2", trade2.sellOrderId());
        assertEquals(new BigDecimal("101.00"), trade2.price());
        assertEquals(new BigDecimal("3"), trade2.quantity());

        // Asks book should have 7 remaining units of O2 at 101.00
        assertEquals(1, orderBook.getAsks().size());
        Order remainingAsk = orderBook.getAsks().iterator().next();
        assertEquals("O2", remainingAsk.getOrderId());
        assertEquals(new BigDecimal("7"), remainingAsk.getRemainingQuantity());
    }

    @Test
    void testTimestampOrdering() {
        Instant now = Instant.now();
        // Two sell orders at the same price but different timestamps
        Order sell1 = new Order("O1", SYMBOL, Side.SELL, OrderType.LIMIT, new BigDecimal("150.00"), new BigDecimal("5"), now.plusMillis(10));
        Order sell2 = new Order("O2", SYMBOL, Side.SELL, OrderType.LIMIT, new BigDecimal("150.00"), new BigDecimal("5"), now); // earlier timestamp
        orderBook.process(sell1);
        orderBook.process(sell2);

        // Incoming buy order of 5 units should match the earlier timestamp (O2)
        Order buy = new Order("O3", SYMBOL, Side.BUY, OrderType.LIMIT, new BigDecimal("150.00"), new BigDecimal("5"), now.plusMillis(20));
        List<Trade> trades = orderBook.process(buy);

        assertEquals(1, trades.size());
        assertEquals("O2", trades.getFirst().sellOrderId()); // matched with earlier timestamp

        // Only O1 should remain in the asks book
        assertEquals(1, orderBook.getAsks().size());
        assertEquals("O1", orderBook.getAsks().iterator().next().getOrderId());
    }
}
