package org.afob.limit;

import org.junit.Assert;
import org.junit.Test;
import org.afob.execution.ExecutionClient;
import org.junit.Before;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.mockito.Mockito.times;

public class LimitOrderAgentTest {

    private ExecutionClient executionClient;
    private LimitOrderAgent limitOrderAgent;

    @Before
    void initializeTest() {
        executionClient = Mockito.mock(ExecutionClient.class);
        limitOrderAgent = new LimitOrderAgent(executionClient);
    }

    @Test
    void testIBMOrderExecutedWhenPriceBelow100() throws ExecutionClient.ExecutionException {
        limitOrderAgent.addOrder(true, "IBM", 1000, new BigDecimal("100"));
        // Price tick that should trigger execution
        limitOrderAgent.priceTick("IBM", 95.50);

        // Verify that the order was executed
        Mockito.verify(executionClient).buy(any());
    }

    @Test
    void testIBMOrderNotExecutedWhenPriceAbove100() throws ExecutionClient.ExecutionException {
        limitOrderAgent.addOrder(true, "IBM", 1000, new BigDecimal("100"));
        // Price tick that should not trigger execution
        limitOrderAgent.priceTick("IBM", 101.50);

        // Verify that the order was not executed
        Mockito.verify(executionClient).buy(any());
    }

}
