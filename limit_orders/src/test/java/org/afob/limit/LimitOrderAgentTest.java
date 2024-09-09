package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.math.BigDecimal;
import static org.mockito.Mockito.*;

public class LimitOrderAgentTest {
    private ExecutionClient mockExecutionClient;
    private LimitOrderAgent limitOrderAgent;

    @BeforeEach
    public void setUp() {
        mockExecutionClient = Mockito.mock(ExecutionClient.class);
        limitOrderAgent = new LimitOrderAgent(mockExecutionClient);
    }

    @Test
    public void testBuyOrderExecutedWhenPriceBelowLimit() throws ExecutionClient.ExecutionException {
        limitOrderAgent.addOrder(true, "IBM", 1000, new BigDecimal("100"));
        limitOrderAgent.priceTick("IBM", new BigDecimal("99"));
        verify(mockExecutionClient, times(1)).buy("IBM", 1000);
    }
    @Test
    public void testBuyOrderNotExecutedWhenPriceAboveLimit() throws ExecutionClient.ExecutionException {
        limitOrderAgent.addOrder(true, "IBM", 1000, new BigDecimal("100"));
        limitOrderAgent.priceTick("IBM", new BigDecimal("101"));
        // Verify that the buy method was not called
        verify(mockExecutionClient, never()).buy(anyString(), anyInt());
    }
    @Test
    public void testSellOrderExecutedWhenPriceAboveLimit() throws ExecutionClient.ExecutionException {
        limitOrderAgent.addOrder(false, "IBM", 500, new BigDecimal("150"));
        limitOrderAgent.priceTick("IBM", new BigDecimal("151"));
        // Verify that the sell method was called
        verify(mockExecutionClient, times(1)).sell("IBM", 500);
    }
}