package org.afob.limit;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

import org.afob.execution.ExecutionClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class LimitOrderAgentTest {

    // @Test
    // public void addTestsHere() {
    // Assert.fail("not implemented");
    // }

    private LimitOrderAgent limitOrderAgent;
    private ExecutionClient executionClient;

    @BeforeEach
    public void setUp() {
        executionClient = Mockito.mock(ExecutionClient.class);
        limitOrderAgent = new LimitOrderAgent(executionClient);
    }

    @Test
    public void testBuyOrderWhenPriceIsBelowfromLimit() throws ExecutionClient.ExecutionException {
        limitOrderAgent.addOrder("IBM", 1000, new BigDecimal("150.00"), true);
        BigDecimal currentPrice = new BigDecimal("149.00");
        limitOrderAgent.priceTick("IBM", currentPrice);
        verify(executionClient, times(1)).buy("IBM", 1000);
    }

    @Test
    public void testSellOrderWhenPriceIsUpFromLimit() throws ExecutionClient.ExecutionException {
        limitOrderAgent.addOrder("IBM", 500, new BigDecimal("150.00"), false);
        BigDecimal currentPrice = new BigDecimal("150.50");
        limitOrderAgent.priceTick("IBM", currentPrice);
        verify(executionClient, times(1)).sell("IBM", 500);
        verify(executionClient, never()).buy(anyString(), anyInt());

    }

    @Test
    public void testOrderWhenPriceConditionDoesNotMet() throws ExecutionClient.ExecutionException {
        limitOrderAgent.addOrder("IBM", 1000, new BigDecimal("100.00"), true);
        BigDecimal currentPrice = new BigDecimal("101.00");
        limitOrderAgent.priceTick("IBM", currentPrice);
        verify(executionClient, never()).buy(anyString(), anyInt());
        verify(executionClient, never()).sell(anyString(), anyInt());
    }

    @Test
    public void testExecuteMultipleOrders() throws ExecutionClient.ExecutionException {
        limitOrderAgent.addOrder("IBM", 1000, new BigDecimal("100.00"), true);
        limitOrderAgent.addOrder("CSG", 500, new BigDecimal("150.00"), false);

        BigDecimal currentPriceIBM = new BigDecimal("99.50");
        BigDecimal currentPriceCSG = new BigDecimal("150.50");

        limitOrderAgent.priceTick("CSG", currentPriceCSG);
        limitOrderAgent.priceTick("IBM", currentPriceIBM);
        verify(executionClient, times(1)).buy("IBM", 1000);
        verify(executionClient, times(1)).sell("CSG", 500);

    }

    @Test
    public void testExceptionHandling() throws ExecutionClient.ExecutionException {
        limitOrderAgent.addOrder("IBM", 1000, new BigDecimal("100.00"), true);
        BigDecimal currentPrice = new BigDecimal("99.50");
        doThrow(new ExecutionClient.ExecutionException("Execution failed")).when(executionClient).buy("IBM", 1000);

        limitOrderAgent.priceTick("IBM", currentPrice);
        verify(executionClient, times(1)).buy("IBM", 1000);

        limitOrderAgent.priceTick("IBM", currentPrice);
        verify(executionClient, times(2)).buy("IBM", 1000);

    }
}
