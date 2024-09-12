package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class LimitOrderAgentTest {

    private ExecutionClient mockExecutionClient;
    private LimitOrderAgent limitOrderAgent;

    @BeforeEach
    public void setUp() {
        mockExecutionClient = mock(ExecutionClient.class);
        limitOrderAgent = new LimitOrderAgent(mockExecutionClient);
    }

    @Test
    public void testAddOrderAndPriceTick() {
        // Add a buy order
        limitOrderAgent.addOrder(true, "IBM", 1000, 100.0);

        // Simulate a price tick below the limit
        limitOrderAgent.priceTick("IBM", 99.0);

        // Verify the execution
        ArgumentCaptor<String> productIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> amountCaptor = ArgumentCaptor.forClass(Integer.class);

        try {
            verify(mockExecutionClient).buy(productIdCaptor.capture(), amountCaptor.capture());
        } catch (ExecutionClient.ExecutionException e) {
            throw new RuntimeException(e);
        }

        assertEquals("IBM", productIdCaptor.getValue());
        assertEquals(Optional.of(1000), amountCaptor.getValue());
    }

    @Test
    public void testAddOrderAndPriceTickSell() {
        // Add a sell order
        limitOrderAgent.addOrder(false, "AAPL", 500, 150.0);

        // Simulate a price tick above the limit
        limitOrderAgent.priceTick("AAPL", 151.0);

        // Verify the execution
        ArgumentCaptor<String> productIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> amountCaptor = ArgumentCaptor.forClass(Integer.class);

        try {
            verify(mockExecutionClient).sell(productIdCaptor.capture(), amountCaptor.capture());
        } catch (ExecutionClient.ExecutionException e) {
            throw new RuntimeException(e);
        }

        assertEquals("AAPL", productIdCaptor.getValue());
        assertEquals(Optional.of(500), amountCaptor.getValue());
    }

    @Test
    public void testOrderNotExecutedWhenPriceDoesNotMeetLimit() {
        // Add a buy order
        limitOrderAgent.addOrder(true, "MSFT", 2000, 250.0);

        // Simulate a price tick above the limit
        limitOrderAgent.priceTick("MSFT", 251.0);

        // Verify that no order is executed
        try {
            verify(mockExecutionClient, never()).buy(anyString(), anyInt());
        } catch (ExecutionClient.ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}