package org.afob.limit;

import org.afob.execution.ExecutionClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class LimitOrderAgentTest {

    private ExecutionClient executionClient;
    private LimitOrderAgent agent;

    @BeforeEach
    public void setUp() {
        executionClient = Mockito.mock(ExecutionClient.class);
        agent = new LimitOrderAgent(executionClient);
    }

    @Test
    public void testBuyOrderExecuted() throws ExecutionClient.ExecutionException {
        String productId = "IBM";
        int amount = 1000;
        BigDecimal limitPrice = BigDecimal.valueOf(100.0);

        // Add a buy order for IBM
        agent.addLimitOrder(productId, amount, limitPrice, true);

        // Simulate a price tick that meets the limit price
        agent.priceTick(productId, BigDecimal.valueOf(99.5));

        // Verify that the buy method was called on the execution client
        verify(executionClient, times(1)).buy(productId, amount);
    }

    @Test
    public void testSellOrderExecuted() throws ExecutionClient.ExecutionException {
        String productId = "IBM";
        int amount = 500;
        double limitPrice = 150.0;

        // Add a sell order for IBM
        agent.addLimitOrder(productId, amount, BigDecimal.valueOf(limitPrice), false);

        // Simulate a price tick that meets the limit price
        agent.priceTick(productId, BigDecimal.valueOf(151.0));

        // Verify that the sell method was called on the execution client
        verify(executionClient, times(1)).sell(productId, amount);
    }

    @Test
    public void testOrderNotExecuted() throws ExecutionClient.ExecutionException {
        String productId = "IBM";
        int amount = 1000;
        double limitPrice = 100.0;

        // Add a buy order for IBM
        agent.addLimitOrder(productId, amount, BigDecimal.valueOf(limitPrice), true);

        // Simulate a price tick that does not meet the limit price
        agent.priceTick(productId, BigDecimal.valueOf(101.0));

        // Verify that no methods were called on the execution client
        verify(executionClient, never()).buy(anyString(), anyInt());
        verify(executionClient, never()).sell(anyString(), anyInt());
    }
}