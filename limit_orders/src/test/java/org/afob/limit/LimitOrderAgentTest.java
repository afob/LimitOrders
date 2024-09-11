package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class LimitOrderAgentTest {

    private ExecutionClient executionClient;
    private LimitOrderAgent limitOrderAgent;

    @Before
    public void setUp() throws Exception {
        //setting up the mock
        executionClient = mock(ExecutionClient.class);
        limitOrderAgent = new LimitOrderAgent(executionClient);
    }

    @Test
    public void testAddOrder() {
        //Add a buy order
        limitOrderAgent.addLimitOrder(new LimitOrderAgent.LimitOrder(LimitOrderAgent.LimitOrder.LimitOrderType.BUY, "IBM", 1000, new BigDecimal(100)));
        limitOrderAgent.priceTick("IBM", new BigDecimal(95));
        try {
            verify(executionClient).buy("IBM", 1000);
        } catch (ExecutionClient.ExecutionException e) {
            Assert.fail("Execution exception shouldn't be thrown");
        }
    }

    @Test
    public void testExecuteBuyLimitOrder() throws ExecutionClient.ExecutionException {
        //Add a buy order
        limitOrderAgent.addLimitOrder(new LimitOrderAgent.LimitOrder(LimitOrderAgent.LimitOrder.LimitOrderType.BUY, "IBM", 1000, new BigDecimal(100)));
        //Trigger the price tick below the limit price to execute the order
        limitOrderAgent.priceTick("IBM", new BigDecimal(99));
        verify(executionClient).buy("IBM", 1000);
        verify(executionClient, never()).sell(anyString(), anyInt());
    }

    @Test
    public void testExecuteSellLimitOrder() throws ExecutionClient.ExecutionException {
        //Add a sell order
        limitOrderAgent.addLimitOrder(new LimitOrderAgent.LimitOrder(LimitOrderAgent.LimitOrder.LimitOrderType.SELL, "IBM", 1000, new BigDecimal(150)));
        //Trigger the price tick above the limit price to execute the order
        limitOrderAgent.priceTick("IBM", new BigDecimal(200));
        verify(executionClient).sell("IBM", 1000);
        verify(executionClient, never()).buy(anyString(), anyInt());
    }


    @Test
    public void testOrderNotExecutedIfLimitPriceDoesNotMatch() throws ExecutionClient.ExecutionException {
        //Add a buy order
        limitOrderAgent.addLimitOrder(new LimitOrderAgent.LimitOrder(LimitOrderAgent.LimitOrder.LimitOrderType.BUY, "BOA", 2000, new BigDecimal(100)));
        limitOrderAgent.priceTick("BOA", new BigDecimal(180));
        //Add a sell order
        limitOrderAgent.addLimitOrder(new LimitOrderAgent.LimitOrder(LimitOrderAgent.LimitOrder.LimitOrderType.SELL, "IBM", 1000, new BigDecimal(150)));
        //Trigger the price tick that do not match the criteria
        limitOrderAgent.priceTick("IBM", new BigDecimal(100));

        verify(executionClient).buy("BOA", 2000);
        verify(executionClient).sell("IBM", 1000);

    }

    @Test
    public void testExceptionHandlingDuringOrderExecution() throws ExecutionClient.ExecutionException {
        //Add a buy order
        limitOrderAgent.addLimitOrder(new LimitOrderAgent.LimitOrder(LimitOrderAgent.LimitOrder.LimitOrderType.BUY, "BOA", 2000, new BigDecimal(100)));
        //setup the mock to throw an exception when buy method is called
        doThrow(new ExecutionClient.ExecutionException("Execution failed")).when(executionClient).buy("BOA", 2000);
        limitOrderAgent.priceTick("BOA", new BigDecimal(90));
        verify(executionClient, times(1)).buy("BOA", 2000);
    }

}