package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LimitOrderAgent implements PriceListener {

    private final List<LimitOrder> orderBook;
    private final ExecutionClient executionClient;

    public LimitOrderAgent(ExecutionClient executionClient) {
        this.orderBook = new ArrayList<>();
        this.executionClient = executionClient;
    }

    // Method to add a new limit order to the order book
    public void addLimitOrder(String symbol, int quantity, BigDecimal limitPrice, boolean isBuyOrder) {
        LimitOrder order = new LimitOrder(isBuyOrder, symbol, quantity, limitPrice);
        orderBook.add(order);
    }

    // Method to handle incoming price ticks
    public void priceTick(String symbol, BigDecimal price) throws ExecutionClient.ExecutionException {
        Iterator<LimitOrder> iterator = orderBook.iterator();
        while (iterator.hasNext()) {
            LimitOrder order = iterator.next();
            if (order.getProductId().equals(symbol) && order.isExecutable(price)) {
                if (order.isBuy()) {
                    executionClient.buy(order.getProductId(), order.getAmount());
                } else {
                    executionClient.sell(order.getProductId(), order.getAmount());
                }
                iterator.remove();
            }
        }
    }
}
