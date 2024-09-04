package org.afob.limit;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

public class LimitOrderAgent implements PriceListener {

        private final ExecutionClient executionClient;
        private final Map<String, Order> orders = new HashMap<>();

       
        public LimitOrderAgent(final ExecutionClient ec) {
            this.executionClient = ec;
        }

       
        public void addOrder(String productId, boolean isBuyOrder, int amount, BigDecimal limitPrice) {
            orders.put(productId, new Order(isBuyOrder, amount, limitPrice));
        }

        @Override
        public void priceTick(String productId, BigDecimal price) {
            if (orders.containsKey(productId)) {
                Order order = orders.get(productId);
                if (order.isBuyOrder && price.compareTo(order.limitPrice) <= 0) {
                    executeOrder(productId, order.amount, price);
                    orders.remove(productId);
                } else if (!order.isBuyOrder && price.compareTo(order.limitPrice) >= 0) {
                    executeOrder(productId, order.amount, price);
                    orders.remove(productId);
                }
            }
        }

        // Method to execute the order
        private void executeOrder(String productId, int amount, BigDecimal price) {
            executionClient.execute(productId, amount, price);
            System.out.println("Executed " + (amount > 0 ? "buy" : "sell") + " order for " + amount + " shares of " + productId + " at " + price);
        }

        // Inner class to store order details
        private static class Order {
            boolean isBuyOrder;
            int amount;
            BigDecimal limitPrice;

            Order(boolean isBuyOrder, int amount, BigDecimal limitPrice) {
                this.isBuyOrder = isBuyOrder;
                this.amount = amount;
                this.limitPrice = limitPrice;
            }
        }

        public static void main(String[] args) {
            ExecutionClient ec = new ExecutionClient();  
            LimitOrderAgent agent = new LimitOrderAgent(ec);

            // buy 1000 shares of IBM if price drops below $100
            agent.addOrder("IBM", true, 1000, new BigDecimal("100.00"));

            // Simulate price ticks
            agent.priceTick("IBM", new BigDecimal("105.00")); 
            agent.priceTick("IBM", new BigDecimal("99.00")); 
        }
    }
