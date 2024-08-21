package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LimitOrderAgent implements PriceListener {

    private final ExecutionClient executionClient;
    private final List<Order> orderList;

    // Constructor
    public LimitOrderAgent(final ExecutionClient executionClient) {
        this.executionClient = executionClient;
        this.orderList = new ArrayList<>();
    }

    // Order Class to store order details
    private static class Order {
        boolean isBuy;
        String productId;
        int amount;
        BigDecimal limitPrice;

        public Order(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
            this.isBuy = isBuy;
            this.productId = productId;
            this.amount = amount;
            this.limitPrice = limitPrice;
        }
    }

    // Method to add orders (buy/sell)
    public void addOrder(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
        Order order = new Order(isBuy, productId, amount, limitPrice);
        orderList.add(order);
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
        // Iterate over orders and check if any can be executed
        List<Order> executedOrders = new ArrayList<>();

        for (Order order : orderList) {
            try {
                if (order.productId.equals(productId)) {
                    if (order.isBuy && price.compareTo(order.limitPrice) <= 0) {
                        // Execute buy order if price is less than or equal to the limit price
                        executionClient.buy(productId, order.amount);
                        executedOrders.add(order);
                    } else if (!order.isBuy && price.compareTo(order.limitPrice) >= 0) {
                        // Execute sell order if price is greater than or equal to the limit price
                        executionClient.sell(productId, order.amount);
                        executedOrders.add(order);
                    }
                }
            } catch (ExecutionException e) {
                // Handle exceptions gracefully without breaking the loop
                System.err.println("Order execution failed for product: " + productId + ". Error: " + e.getMessage());
            }
        }

        // Remove executed orders from the list
        orderList.removeAll(executedOrders);
    }
}
