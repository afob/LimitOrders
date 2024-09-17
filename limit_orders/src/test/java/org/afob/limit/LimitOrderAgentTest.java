package org.afob.limit;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LimitOrderAgentTest {

    private final double IBM_MKT_PRICE= 98.02;
    OrderService orderService= new OrderService();

    @Test
    public void placeIBMOrderWhenLimtAboveMktPrice() {
//        ExecutionClient ec = new ExecutionClient();
//
//        assertThrows(ExecutionClient.ExecutionException.class,
//                ()-> {
//                    ec.buy("IBM", 100);
//                });


        List<Order> orders= new ArrayList<>();
        orders.add(new Order(true, "IBM", 1000, 100.0));

        for(Order order: orders){
            orderService.addOrder(order.getBuyOrSell(), order.getProductId(), order.getAmount(), order.getLimit());
            assertEquals("SUCCESSFULLY BOUGHT", orderService.executeOrders());
        }

    }
    @Test
    public void placeIBMOrderWhenLimtBelowMktPrice() {
        List<Order>  orders= new ArrayList<>();
        orders.add(new Order(true, "IBM", 5000, 95.0));

        for(Order order: orders){
            orderService.addOrder(order.getBuyOrSell(), order.getProductId(), order.getAmount(), order.getLimit());
            assertEquals("FAILED TO PLACE ORDER LIMIT DOESN'T MET", orderService.executeOrders());
        }

    }
}