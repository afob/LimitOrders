package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class LimitOrderAgentTest {
    private ExecutionClient executionClient;
    private LimitOrderAgent agent;

    @Before
    public void setUp() {
        executionClient = mock(ExecutionClient.class);
        agent = new LimitOrderAgent(executionClient);
    }

    @Test
    public void testBuyOrderExecutedWhenPriceIsBelowLimit() throws ExecutionException {
        agent.addOrder(true, "IBM", 1000, new BigDecimal("100"));

        agent.priceTick("IBM", new BigDecimal("99"));

        verify(executionClient).buy("IBM", 1000);
    }

    @Test
    public void testSellOrderExecutedWhenPriceIsAboveLimit() throws ExecutionException {
        agent.addOrder(false, "IBM", 1000, new BigDecimal("100"));

        agent.priceTick("IBM", new BigDecimal("101"));

        verify(executionClient).sell("IBM", 1000);
    }

    @Test
    public void testNoExecutionWhenPriceIsNotAtLimit() throws ExecutionException {
        agent.addOrder(true, "IBM", 1000, new BigDecimal("100"));

        agent.priceTick("IBM", new BigDecimal("101"));

        verify(executionClient, never()).buy(anyString(), anyInt());
        verify(executionClient, never()).sell(anyString(), anyInt());
    }

    @Test
    public void testOrderNotExecutedTwice() throws ExecutionException {
        agent.addOrder(true, "IBM", 1000, new BigDecimal("100"));

        agent.priceTick("IBM", new BigDecimal("99"));
        agent.priceTick("IBM", new BigDecimal("98"));

        verify(executionClient, times(1)).buy("IBM", 1000);
    }
}
