# Order Matching Engine

A high-performance, single-threaded (per symbol) order matching engine implemented in Java 21.

## Project Architecture

The Day 1 implementation lays the core foundation of a price-time priority matching engine. It is divided into two main layers:

1. **Domain Model (`com.engine.model`)**: Contains core entities representing trading data.
   - `Side`: Enum representing `BUY` or `SELL`.
   - `OrderType`: Enum representing `LIMIT` or `MARKET`.
   - `Order`: Represents a trade request. Handles partial or full fills via `fill()`.
   - `Trade`: A Java 21 `record` capturing the execution details of matched orders (buyer, seller, execution price, quantity, and timestamp).

2. **Core Matching Engine (`com.engine`)**:
   - `OrderBook`: Maintains the state of resting bid and ask orders. It uses two `TreeSet` collections with custom comparators to ensure strict price-time priority.

```
       Incoming Order
             │
             ▼
    ┌─────────────────┐
    │    OrderBook    │
    └────────┬────────┘
             │
      ┌──────┴──────┐
      ▼             ▼
  Bids Set      Asks Set
 (Price DESC   (Price ASC
  Time ASC)     Time ASC)
```

## Public Interfaces

### `OrderBook`
- `OrderBook(String symbol)`: Constructs a new order book.
- `List<Trade> process(Order incomingOrder)`: Processes an incoming order. Matches it against the opposite side. Any remaining quantity of a limit order is rested on the book, while market orders are cancelled.
- `Set<Order> getBids()`: Returns an unmodifiable view of the resting bids.
- `Set<Order> getAsks()`: Returns an unmodifiable view of the resting asks.
- `void clear()`: Empties the order book.

### `Order`
- `Order(String orderId, String symbol, Side side, OrderType type, BigDecimal price, BigDecimal quantity, Instant timestamp)`
- Getters for metadata: `getOrderId()`, `getSymbol()`, `getSide()`, `getType()`, `getPrice()`, `getQuantity()`, `getRemainingQuantity()`, `getTimestamp()`.
- State tracking: `isFilled()`, `fill(BigDecimal quantity)`.

## Day 2 Continuation Guide

For developers looking to continue on Day 2:
1. **Concurrency Support**: Currently, `OrderBook` uses `synchronized` on public methods for thread safety. Consider using finer-grained locking (e.g., read-write locks or Disruptor-style lock-free ring buffers) for higher throughput if multi-threaded execution is desired.
2. **Order Cancellations / Modifications**: Implement `cancelOrder(String orderId)` and `modifyOrder(String orderId, BigDecimal newPrice, BigDecimal newQty)` by maintaining an index map from `orderId` to `Order` for $O(1)$ lookups.
3. **Advanced Order Types**: Support Good-Till-Cancelled (GTC), Immediate-Or-Cancel (IOC), Fill-Or-Kill (FOK), and Stop orders.
4. **Multi-Asset Routing**: Create an `EngineRouter` mapping symbols to their corresponding `OrderBook` to allow parallel matching across multiple instruments.
