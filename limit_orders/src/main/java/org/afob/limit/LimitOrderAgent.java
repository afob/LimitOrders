package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LimitOrderAgent implements PriceListener {
    
    private final ExecutionClient executionClient;
    private final List<Order> orders;
    
    public LimitOrderAgent(final ExecutionClient ec) {
        this.executionClient = ec;
        this.orders = new ArrayList<Order>();
    }

    public void addOrder(String productId, int amount, BigDecimal limit, boolean isBuy) {
        Order order = new Order(isBuy, productId, amount, limit);
        orders.add(order);
    }
    
    @Override
    public void priceTick(String productId, BigDecimal price) {
        Iterator<Order> iterator = orders.iterator();
        while (iterator.hasNext()) {
            Order order = iterator.next();
            if (order.getProductId().equals(productId)) {
                boolean isPriceConditionMet = order.isBuy() ? price.compareTo(order.getLimit()) <= 0
                        : price.compareTo(order.getLimit()) >= 0;

                if (isPriceConditionMet) {
                    try {
                        if (order.isBuy()) {
                            executionClient.buy(productId, order.getAmount());
                        } else {
                            executionClient.sell(productId, order.getAmount());
                        }
                        iterator.remove();
                    } catch (ExecutionClient.ExecutionException e) {
                        System.err.println("Order execution failed " + e.getMessage());
                    }
                }
            }
        }        

    }

}
public static class Order {
    private final boolean isBuy;
    private final String productId;
    private final int amount;
    private final BigDecimal limit;

    public Order(boolean isBuy,String productId, int amount,BigDecimal limit){
        this.isBuy = isBuy;
        this.limit = limit;
        this.productId = productId;
        this.amount = amount;
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
    public BigDecimal getLimit() {
        return limit;
    }
