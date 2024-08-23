package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.mockito.Mockito.times;

public class LimitOrderAgentTest {

    private ExecutionClient executionClient;
    private LimitOrderAgent limitOrderAgent;
    @Before
    public void setUp() {
        executionClient = Mockito.mock(ExecutionClient.class);
        limitOrderAgent = new LimitOrderAgent(executionClient);
    }

    @Test
    public void shouldHaveDefaultOrderTest() throws ExecutionClient.ExecutionException {
        // Simulate a price tick below $100
        limitOrderAgent.priceTick("IBM", new BigDecimal("99.99"));
        // Verify that buy method is called with correct parameters
        Mockito.verify(executionClient).buy("IBM", 1000);
    }

    @Test
    public void shouldExecuteBuyOrderTest1() throws ExecutionClient.ExecutionException {
        // Add a custom order
        limitOrderAgent.addOrder(true, "AAPL", 500, new BigDecimal("150"));
        // Simulate a price tick that should trigger the order
        limitOrderAgent.priceTick("AAPL", new BigDecimal("149.99"));
        // Verify that buy method is called for AAPL
        Mockito.verify(executionClient).buy("AAPL", 500);
    }

    @Test
    public void shouldNotExecuteBuyOrderTest() throws ExecutionClient.ExecutionException {
        // Add an order with a limit price
        limitOrderAgent.addOrder(true, "GOOG", 200, new BigDecimal("2500"));
        // Simulate a price tick that meets the limit
        limitOrderAgent.priceTick("GOOG", new BigDecimal("2600"));
        // Verify that the buy method is not called
        Mockito.verify(executionClient, times(0)).buy("GOOG", 2600);
    }

    @Test
    public void shouldRemoveExecutedOrdersTest() throws ExecutionClient.ExecutionException {
        // Add an order and execute it
        limitOrderAgent.addOrder(true, "MSFT", 300, new BigDecimal("300"));

        // Simulate a price tick that meets the limit
        limitOrderAgent.priceTick("MSFT", new BigDecimal("300"));

        // Verify that the buy method is called
        Mockito.verify(executionClient).buy("MSFT", 300);

        // Simulate another price tick for the same product
        limitOrderAgent.priceTick("MSFT", new BigDecimal("300"));

        // Verify that the buy method is not called again (order should be removed)
        Mockito.verify(executionClient, times(1)).buy("MSFT", 300);
        Assert.assertEquals(limitOrderAgent.orders.size(), 1);
        Assert.assertNotEquals("MSFT", limitOrderAgent.orders.get(0).getProductId()); // only has default "IMB" productId
    }
}