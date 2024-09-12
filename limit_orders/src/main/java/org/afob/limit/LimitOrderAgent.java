package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LimitOrderAgent implements PriceListener {

    private final ExecutionClient executionClient;
    private final List<Order> orders = new ArrayList<>();



    public LimitOrderAgent(ExecutionClient executionClient) {
        this.executionClient = executionClient;
    }

    public void addOrder(boolean buyOrSell, String productId, int amount, double limitPrice) {
        Order order = new Order(buyOrSell, productId, amount, limitPrice);
        orders.add(order);
    }

    public void priceTick(String productId, double price) {
        // Use a separate list to collect orders that need to be removed
        List<Order> toRemove = new ArrayList<>();

        for (Order order : orders) {
            if (order.getProductId().equals(productId)) {
                if ((order.isBuy() && price <= order.getLimitPrice()) ||
                        (!order.isBuy() && price >= order.getLimitPrice())) {
                    executeOrder(order);
                    toRemove.add(order); // Add to the removal list
                }
            }
        }

        // Remove orders after processing
        orders.removeAll(toRemove);
    }

    private void executeOrder(Order order) {
        if (order.isBuy()) {
            try {
                executionClient.buy(order.getProductId(), order.getAmount());
            } catch (ExecutionClient.ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                executionClient.sell(order.getProductId(), order.getAmount());
            } catch (ExecutionClient.ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {

    }
}
