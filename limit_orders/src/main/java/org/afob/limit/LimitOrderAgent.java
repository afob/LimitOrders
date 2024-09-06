package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LimitOrderAgent implements PriceListener {

    private static class Order {
         boolean isBuyOrder;
        String productId;
        int amount;
        BigDecimal limitPrice;

        Order(boolean isBuyOrder, String productId, int amount, BigDecimal limitPrice){
            this.isBuyOrder = isBuyOrder;
            this.productId = productId;
            this.amount = amount;
            this.limitPrice = limitPrice;
        }
    }

    private final ExecutionClient executionClient;
    private final List<Order> orders = new ArrayList<>();

    public LimitOrderAgent(final ExecutionClient ec) {
        this.executionClient = ec;
    }

    public void addOrder(boolean isBuyOrder, String productId, int amount, BigDecimal limitPrice) {
        orders.add(new Order(isBuyOrder, productId, amount, limitPrice));
    }
    


    @Override
    public void priceTick(String productId, BigDecimal price) {

        List<Order> executedOrders = new ArrayList<>();

        for(Order order : orders){
            if((order.productId).equals(productId)){
                if(order.isBuyOrder && price.compareTo(order.limitPrice) <= 0){
                    try{
                        executionClient.buy(productId, order.amount);
                        executedOrders.add(order); //Marking for removal
                    } catch (ExecutionException e){
                        System.err.println("Buy order execution failed"+ e.getMessage());
                    }
                } else if( !order.isBuyOrder && price.compareTo(order.limitPrice) >= 0) {
                    try{
                        executionClient.sell(productId, order.amount);
                        executedOrders.add(order); //Marking for removal 
                    } catch (ExecutionException e) {
                        System.err.println("Failed to execute sell order: " + e. getMessage());
                    }
                }
            }
        }
        orders.removeAll(executedOrders); //Removing the executed orders
    }

}

// Also, regarding enhancements of trading-framework we need to enhance the buy and sell  methods to implement retry mechanisms and to mitigate external failures so that it doesn't throw any exception.
//book management, order prioritization and error recovery should also be added to be production ready
