package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;

import static org.mockito.Mockito.doThrow;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ExecutionClient.class)
public class LimitOrderAgentTest {

    private ExecutionClient executionClient;
    private LimitOrderAgent agent;

    @Before
    public void setUp() {
        // Mock the final class ExecutionClient
        executionClient = PowerMockito.mock(ExecutionClient.class);
        agent = new LimitOrderAgent(executionClient);
    }

    @Test
    public void testExecuteBuyOrder() throws ExecutionClient.ExecutionException {
        agent.addOrder(Order.Type.BUY, "IBM", 1000, BigDecimal.valueOf(100.00));
        agent.priceTick("IBM", BigDecimal.valueOf(99.50));
        Mockito.verify(executionClient).buy("IBM", 1000);
    }

    @Test
    public void testExecuteSellOrder() throws ExecutionClient.ExecutionException {
        agent.addOrder(Order.Type.SELL, "IBM", 1000, BigDecimal.valueOf(100.00));
        agent.priceTick("IBM", BigDecimal.valueOf(101.50));
        Mockito.verify(executionClient).sell("IBM", 1000);
    }

    @Test
    public void testBuyOrderFailure() throws ExecutionClient.ExecutionException {
        doThrow(new ExecutionClient.ExecutionException("failed to buy")).when(executionClient).buy(Mockito.anyString(), Mockito.anyInt());
        agent.addOrder(Order.Type.BUY, "IBM", 1000, BigDecimal.valueOf(100.00));
        agent.priceTick("IBM", BigDecimal.valueOf(99.50));
        // Add your verification or assertions here
    }
}
