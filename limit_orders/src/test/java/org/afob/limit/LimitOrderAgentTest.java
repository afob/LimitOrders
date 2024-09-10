package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class LimitOrderAgentTest {
    private ExecutionClient executionClient;
    private LimitOrderAgent limitOrderAgent;

    @Before
    public void setUp() {
        executionClient = mock(ExecutionClient.class);
        limitOrderAgent = new LimitOrderAgent(executionClient);
    }

    @Test
    public void testBuyOrderIfPriceIsBelowLimit() throws ExecutionException {
    	limitOrderAgent.addOrder(true, "IBM", 1000, new BigDecimal("100"));

    	limitOrderAgent.priceTick("IBM", new BigDecimal("99"));

        verify(executionClient).buy("IBM", 1000);
    }

    @Test
    public void testSellOrderIfPriceIsAboveLimit() throws ExecutionException {
    	limitOrderAgent.addOrder(false, "IBM", 1000, new BigDecimal("100"));

        limitOrderAgent.priceTick("IBM", new BigDecimal("101"));

        verify(executionClient).sell("IBM", 1000);
    }

    @Test
    public void testNoBuyOrderIfPriceIsNotAtLimit() throws ExecutionException {
    	limitOrderAgent.addOrder(true, "IBM", 1000, new BigDecimal("100"));

    	limitOrderAgent.priceTick("IBM", new BigDecimal("101"));

        verify(executionClient, never()).buy(anyString(), anyInt());
    }
    
    @Test
    public void testNoSellOrderIfPriceIsNotAtLimit() throws ExecutionException {
    	limitOrderAgent.addOrder(false, "IBM", 1000, new BigDecimal("100"));

    	limitOrderAgent.priceTick("IBM", new BigDecimal("99"));
        
        verify(executionClient, never()).sell(anyString(), anyInt());
    }

    @Test
    public void testBuyOrderWasOnHold() throws ExecutionException {
    	limitOrderAgent.addOrder(true, "IBM", 1000, new BigDecimal("100"));
    	limitOrderAgent.addOrder(true, "IBM", 1000, new BigDecimal("100"));
    	limitOrderAgent.priceTick("IBM", new BigDecimal("99"));
        verify(executionClient,times(2)).buy("IBM", 1000);
    }
    
    @Test
    public void testSellOrderWasOnHold() throws ExecutionException {
    	limitOrderAgent.addOrder(false, "IBM", 1000, new BigDecimal("100"));
    	limitOrderAgent.addOrder(false, "IBM", 1000, new BigDecimal("100"));
    	limitOrderAgent.priceTick("IBM", new BigDecimal("105"));
        verify(executionClient,times(2)).sell("IBM", 1000);
    }
}
