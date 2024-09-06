package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.limit.LimitOrderAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;


import static org.mockito.Mockito.*;

public class LimitOrderAgentTest {

    private ExecutionClient mockExecutionClient;
    private LimitOrderAgent limitOrderAgent;

    @BeforeEach
    public void setUp(){
        mockExecutionClient = Mockito.mock(ExecutionClient.class);
        limitOrderAgent = new LimitOrderAgent(mockExecutionClient);
    }

    @Test
    public void testBuyOrderExecutedWhenPriceDropsBelowLimit() throws ExecutionException{
        limitOrderAgent.addOrder(true, "IBM", 1000, BigDecimal.valueOf(100));
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(99));
        verify(mockExecutionClient, times(1)).buy("IBM", 1000);
    }

    @Test
    public void testBuyOrderNotExecutedWhenPriceAboveLimit() throws ExecutionException{
        limitOrderAgent.addOrder(true, "IBM", 1000, BigDecimal.valueOf(100));
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(101));
        verify(mockExecutionClient, never()).buy("IBM", 1000);
    }

    @Test
    public void testSellOrderExecutedWhenPriceRisesAboveLimit() throws ExecutionException{
        limitOrderAgent.addOrder(false, "IBM", 1000, BigDecimal.valueOf(150));
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(151));
        verify(mockExecutionClient, times(1)).sell("IBM", 1000);
    }

    @Test
    public void testSellOrderNotExecutedWhenPriceBelowLimit() throws ExecutionException{
        limitOrderAgent.addOrder(false, "IBM", 1000, BigDecimal.valueOf(150));
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(149));
        verify(mockExecutionClient, never()).sell("IBM", 1000);
    }

    @Test
    public void testOrderIsExecutedAndThenRemoved() throws ExecutionException{
        limitOrderAgent.addOrder(true, "IBM", 1000, BigDecimal.valueOf(100));
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(99));
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(98));
        verify(mockExecutionClient, times(1)).buy("IBM", 1000);
    }


     @Test
    public void testMultipleOrdersExecuted() throws ExecutionException{
        limitOrderAgent.addOrder(true, "IBM", 500, BigDecimal.valueOf(100));
        limitOrderAgent.addOrder(false, "IBM", 200, BigDecimal.valueOf(150));
        
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(99));
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(151));
        
        verify(mockExecutionClient, times(1)).buy("IBM", 500);
        verify(mockExecutionClient, times(1)).sell("IBM", 200);
    }

    @Test
    public void testExecutionExceptionHandling() throws ExecutionException{
        limitOrderAgent.addOrder(true, "IBM", 1000, BigDecimal.valueOf(100));
        doThrow(new ExecutionException("Failed to buy")).when(mockExecutionClient).buy("IBM", 1000);
        
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(99));
        
        verify(mockExecutionClient, times(1)).buy("IBM", 1000);
    }
    
}
