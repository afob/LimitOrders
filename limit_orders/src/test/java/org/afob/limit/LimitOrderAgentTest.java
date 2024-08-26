package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.afob.limit.Orders;
import org.afob.limit.LimitOrderAgent;

import java.math.BigDecimal;

public class LimitOrderAgentTest {

    private ExecutionClient executionClient;
    private LimitOrderAgent agent;

    @Before
    public void setUp() {
        executionClient = new ExecutionClient() {
            @Override
            public void buy(String productId, int amount) {
                System.out.println("Buy order executed for " + amount + " of " + productId);
            }

            @Override
            public void sell(String productId, int amount) {
                System.out.println("Sell order executed for " + amount + " of " + productId);
            }
        };
        agent = new LimitOrderAgent(executionClient);
    }

    @Test
    public void testPriceTickExecutesMatchingOrders() {
        Order buyOrder = new Order("PRODUCT1", OrderType.BUY, new BigDecimal("100.0"), 10);
        Order sellOrder = new Order("PRODUCT1", OrderType.SELL, new BigDecimal("95.0"), 10);

        agent.submitOrder(buyOrder);
        agent.submitOrder(sellOrder);


        agent.priceTick("PRODUCT1", new BigDecimal("98.0"));

        Assert.assertEquals(0, buyOrder.getQuantity());
        Assert.assertEquals(0, sellOrder.getQuantity());
    }

    @Test
    public void testPriceTickDoesNotExecuteWhenPricesDoNotMatch() {
        Order buyOrder = new Order("PRODUCT1", OrderType.BUY, new BigDecimal("100.0"), 10);
        Order sellOrder = new Order("PRODUCT1", OrderType.SELL, new BigDecimal("105.0"), 10);

        agent.submitOrder(buyOrder);
        agent.submitOrder(sellOrder);


        agent.priceTick("PRODUCT1", new BigDecimal("102.0"));


        Assert.assertEquals(10, buyOrder.getQuantity());
        Assert.assertEquals(10, sellOrder.getQuantity());
    }
}