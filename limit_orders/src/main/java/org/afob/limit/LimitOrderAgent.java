package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.afob.limit.Orders;

public class LimitOrderAgent implements PriceListener {

    private final ExecutionClient executionClient;
    private final PriorityBlockingQueue<Order> buyOrders;
    private final PriorityBlockingQueue<Order> sellOrders;
    private final Lock lock = new ReentrantLock();

    public LimitOrderAgent(final ExecutionClient ec) {
        this.executionClient = ec;
        this.buyOrders = new PriorityBlockingQueue<>((o1, o2) -> o2.getPrice().compareTo(o1.getPrice())); // Highest price first
        this.sellOrders = new PriorityBlockingQueue<>((o1, o2) -> o1.getPrice().compareTo(o2.getPrice())); // Lowest price first
    }

    public void submitOrder(Order order) {
        lock.lock();
        try {
            if (order.getType() == OrderType.BUY) {
                buyOrders.add(order);
            } else {
                sellOrders.add(order);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
        lock.lock();
        try {
            while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
                Order buyOrder = buyOrders.peek();
                Order sellOrder = sellOrders.peek();

                
                if (buyOrder.getProductId().equals(productId) && buyOrder.getPrice().compareTo(price) >= 0 &&
                    sellOrder.getProductId().equals(productId) && sellOrder.getPrice().compareTo(price) <= 0) {

                    int quantityToTrade = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

                    try {
                        executionClient.buy(buyOrder.getProductId(), quantityToTrade);
                        executionClient.sell(sellOrder.getProductId(), quantityToTrade);

                        buyOrder.setQuantity(buyOrder.getQuantity() - quantityToTrade);
                        sellOrder.setQuantity(sellOrder.getQuantity() - quantityToTrade);


                        if (buyOrder.getQuantity() == 0) {
                            buyOrders.poll();
                        }
                        if (sellOrder.getQuantity() == 0) {
                            sellOrders.poll();
                        }
                    } catch (ExecutionException e) {
                        System.err.println("Execution failed: " + e.getMessage());
                        break;
                    }
                } else {
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }
}