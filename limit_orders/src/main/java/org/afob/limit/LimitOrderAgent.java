package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LimitOrderAgent implements PriceListener {

    private final ExecutionClient executionClient;
    protected List<Orders> orders = new ArrayList<>();

    LimitOrderAgent(ExecutionClient client) {

        this.executionClient = client;
        // Automatically add a buy order for 1000 shares of IBM when price drops below $100
        addOrder(true, "IBM", 1000, new BigDecimal("100"));
    }

    /**
     * Method to add a new limit order.
     *
     * @param isBuy      true for buy order, false for sell order
     * @param productId  the product identifier
     * @param amount     the amount to buy or sell
     * @param limitPrice the price at which to execute the order
     */
    public void addOrder(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
        Orders order = new Orders(isBuy, productId, amount, limitPrice);
        orders.add(order);
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
        Iterator<Orders> iterator = orders.iterator();
        // Iterate through all added orders and execute if conditions are met
        while (iterator.hasNext()) {
            Orders order = iterator.next();
            if (order.getProductId().equals(productId)) {
                boolean shouldExecute = (order.isBuy() && price.compareTo(order.getLimitPrice()) <= 0) ||
                        (!order.isBuy() && price.compareTo(order.getLimitPrice()) >= 0);
                if (shouldExecute) {
                    try {
                        if (order.isBuy()) {
                            executionClient.buy(order.getProductId(), order.getAmount());
                        } else {
                            executionClient.sell(order.getProductId(), order.getAmount());
                        }
                        System.out.println("Executed order: " + order);
                        // Remove executed orders from the original list
                        iterator.remove();
                    } catch (ExecutionClient.ExecutionException e) {
                        System.err.println("Failed to execute order " + order + ": " + e.getMessage());
                    }
                }
            }
        }
    }


}
