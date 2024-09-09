package org.afob.limit;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.afob.execution.ExecutionClient;
import org.afob.limit.LimitOrderAgent.OrderType;
import org.junit.Test;
import org.junit.Before;
import org.junit.jupiter.api.Test;

public class LimitOrderAgentTest {
	private ExecutionClient mockExecutionClient;
    private LimitOrderAgent limitOrderAgent;

    @Before
    public void setUp() {
        mockExecutionClient = mock(ExecutionClient.class);
        limitOrderAgent = new LimitOrderAgent(mockExecutionClient);
    }

    @Test
    public void testBuyOrderExecutedWhenPriceFallsBelowLimit() {
        limitOrderAgent.addOrder(OrderType.BUY, "IBM", 1000, new BigDecimal("100.00"));
        limitOrderAgent.priceTick("IBM", new BigDecimal("99.50"));
        verify(mockExecutionClient).executeOrder("IBM", 1000, OrderType.BUY);
    }

    @Test
    public void testSellOrderExecutedWhenPriceRisesAboveLimit() {
        limitOrderAgent.addOrder(OrderType.SELL, "TCS", 500, new BigDecimal("150.00"));
        limitOrderAgent.priceTick("AAPL", new BigDecimal("151.00"));
        verify(mockExecutionClient).executeOrder("TCS", 500, OrderType.SELL);
    }

    @Test
    public void testNoActionWhenPriceDoesNotMeetConditions() {
        limitOrderAgent.addOrder(OrderType.BUY, "IBM", 1000, new BigDecimal("100.00"));
        limitOrderAgent.priceTick("IBM", new BigDecimal("101.00"));
        verify(mockExecutionClient, never()).executeOrder(anyString(), anyInt(), any());
    }

    @Test
    public void testNoActionForUnrelatedProduct() {
        limitOrderAgent.addOrder(OrderType.BUY, "IBM", 1000, new BigDecimal("100.00"));
        limitOrderAgent.priceTick("AAPL", new BigDecimal("99.00"));
        verify(mockExecutionClient, never()).executeOrder(anyString(), anyInt(), any());
    }

    @Test
    public void testMultipleOrdersForSameProduct() {
        limitOrderAgent.addOrder(OrderType.BUY, "IBM", 1000, new BigDecimal("100.00"));
        limitOrderAgent.addOrder(OrderType.SELL, "IBM", 500, new BigDecimal("105.00"));
        limitOrderAgent.priceTick("IBM", new BigDecimal("99.00"));
        verify(mockExecutionClient).executeOrder("IBM", 1000, OrderType.BUY);
        verify(mockExecutionClient, never()).executeOrder("IBM", 500, OrderType.SELL);
        limitOrderAgent.priceTick("IBM", new BigDecimal("106.00"));
        verify(mockExecutionClient).executeOrder("IBM", 500, OrderType.SELL);
    }
}