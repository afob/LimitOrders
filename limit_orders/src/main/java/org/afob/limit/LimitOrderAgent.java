package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class LimitOrderAgent implements PriceListener {
    private static final Logger LOGGER = Logger.getLogger(LimitOrderAgent.class.getName());
    private final ExecutionClient executionClient;
    private final Map<String, List<Order>> orderMap = new HashMap<>();

    public LimitOrderAgent(final ExecutionClient executionClient) {
        this.executionClient = executionClient;
    }

    private static class Order {
        private final boolean isBuy;
        private final String productId;
        private final int amount;
        private final BigDecimal limitPrice;

        public Order(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
            this.isBuy = isBuy;
            this.productId = productId;
            this.amount = amount;
            this.limitPrice = limitPrice;
        }

        public boolean isBuy() {
            return isBuy;
        }

        public String getProductId() {
            return productId;
        }

        public int getAmount() {
            return amount;
        }

        public BigDecimal getLimitPrice() {
            return limitPrice;
        }
    }

    public void addOrder(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
        if (amount <= 0 || limitPrice == null || limitPrice.compareTo(BigDecimal.ZERO) < 0) {
            LOGGER.warning("Invalid order parameters");
            return;
        }
        Order order = new Order(isBuy, productId, amount, limitPrice);
        orderMap.computeIfAbsent(productId, k -> new ArrayList<>()).add(order);
    }

    public void cancelOrder(String productId, int amount) {
        List<Order> orders = orderMap.get(productId);
        if (orders == null) {
            return;
        }

        orders.removeIf(order -> order.getAmount() == amount);
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
        List<Order> orders = orderMap.get(productId);
        if (orders == null) {
            return;
        }

        List<Order> executedOrders = new ArrayList<>();
        for (Order order : orders) {
            if ((order.isBuy() && price.compareTo(order.getLimitPrice()) <= 0) ||
                    (!order.isBuy() && price.compareTo(order.getLimitPrice()) >= 0)) {
                try {
                    if (order.isBuy()) {
                        executionClient.buy(order.getProductId(), order.getAmount());
                    } else {
                        executionClient.sell(order.getProductId(), order.getAmount());
                    }
                    executedOrders.add(order);
                } catch (ExecutionException e) {
                    LOGGER.severe("Order execution failed for product: " + order.getProductId() + ". Error: " + e.getMessage());
                }
            }
        }
        orders.removeAll(executedOrders);
    }
}