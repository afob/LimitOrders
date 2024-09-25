package org.afob.limit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

public class LimitOrderAgent implements PriceListener{

    private List<Order> orders;  // List to store limit orders
    private ExecutionClient executionClient;  // Simulated execution client

    public LimitOrderAgent(ExecutionClient executionClient) {
        this.orders = new ArrayList<>();
        this.executionClient = executionClient;
    }

    // Method to add a limit order (buy/sell)
    public void addOrder(Order.Type type, String productId, int amount, BigDecimal limitPrice) {
        orders.add(new Order(type, productId, amount, limitPrice));
    }
    
    // Method to get the count of current orders
    public int getOrdersCount() {
        return orders.size();
    }
    
    // Process price tick and execute orders if conditions are met
    public void priceTick(String productId, BigDecimal price) {
        if (productId == null || price == null) {
            throw new IllegalArgumentException("Product ID and price cannot be null.");
        }

        Iterator<Order> iterator = orders.iterator();
        
        while (iterator.hasNext()) {
            Order order = iterator.next();
            
            // Check if the order is triggered by the current price
            if (order.getProductId().equals(productId)) {
                boolean isBuyOrder = order.getType() == Order.Type.BUY;
                boolean isTriggered = (isBuyOrder && price.compareTo(order.getLimitPrice()) <= 0) ||
                                      (!isBuyOrder && price.compareTo(order.getLimitPrice()) >= 0);

                if (isTriggered) {
                    try {
                        if (isBuyOrder) {
                            executionClient.buy(productId, order.getAmount());
                        } else {
                            executionClient.sell(productId, order.getAmount());
                        }
                        System.out.println("Executed: " + order);
                        
                        // Remove the executed order
                        iterator.remove();
                    } catch (ExecutionClient.ExecutionException e) {
                        System.err.println("Execution failed: " + e.getMessage());
                    }
                }
            }
        }
    }



    // Example usage
    public static void main(String[] args) {
        ExecutionClient executionClient = new ExecutionClient(); // Simulated client
        LimitOrderAgent agent = new LimitOrderAgent(executionClient);

        // Add a limit order to buy 1000 shares of IBM when the price drops below $100
        agent.addOrder(Order.Type.BUY, "IBM", 1000, BigDecimal.valueOf(100.00));

        // Simulate market price ticks
        BigDecimal[] marketPrices = {
                BigDecimal.valueOf(101.50),
                BigDecimal.valueOf(99.50),
                BigDecimal.valueOf(100.50),
                BigDecimal.valueOf(98.00)
            };
        for (BigDecimal price : marketPrices) {
            agent.priceTick("IBM", price);
        }
    }
}
