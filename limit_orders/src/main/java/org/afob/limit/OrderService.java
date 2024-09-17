package org.afob.limit;

import org.afob.execution.ExecutionClient;

import java.math.BigDecimal;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class OrderService {

    LimitOrderAgent limitOrderAgent;
    private static BlockingQueue<Order> orders;

    public OrderService() {
        this.orders = new LinkedBlockingDeque<>();
        limitOrderAgent = new LimitOrderAgent(new ExecutionClient());
    }

    public void addOrder(boolean buyOrSell, String productId, int amount, Double limit){
        //Logic for checking the current price of product in Market
        Order newOrder = new Order(buyOrSell, productId, amount, limit);
        orders.offer(newOrder);
    }
    public String executeOrders() {
        while(!orders.isEmpty()) {
            Order order= orders.poll();
            BigDecimal priceInBD= new BigDecimal(order.getLimit());
            if(limitOrderAgent.priceTick(order.getProductId(), priceInBD) == 1 ) {
                return limitOrderAgent.executeOrder(order);
            }else {
                //Put the current order to the end of the queue
                //Because in future it might be possible that the price may go down
                synchronized (this) {
                    orders.offer(order);
                }
                return "FAILED TO PLACE ORDER LIMIT DOESN'T MET";
            }
        }
        return "SOMETHING WENT WRONG";
    }


}
