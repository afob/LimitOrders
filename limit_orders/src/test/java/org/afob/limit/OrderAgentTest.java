package org.afob.limit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class OrderAgentTest {
    private ExecutionService executionService;
    private OrderAgent orderAgent;

    @BeforeEach
    public void setUp() {
        executionService = mock(ExecutionService.class);
        orderAgent = new OrderAgent(executionService);
    }

    @Test
    public void testExecuteBuyOrder_Success() throws Exception {
        LimitOrders buyOrder = new LimitOrders(true, "product1", 10, BigDecimal.valueOf(100));
        orderAgent.addLimitOrder(buyOrder);
        orderAgent.executeOrders("product1", BigDecimal.valueOf(99));
        verify(executionService).buy("product1", 10);

    }

    @Test
    public void testExecuteSellOrder_Success() throws Exception {
        LimitOrders sellOrder = new LimitOrders(false, "product1", 10, BigDecimal.valueOf(100));
        orderAgent.addLimitOrder(sellOrder);

        orderAgent.executeOrders("product1", BigDecimal.valueOf(100));

        verify(executionService).sell("product1", 10);
    }

    @Test
    public void testNoExecutionForBuyOrder_WhenPriceIsHigher() throws Exception {
        LimitOrders buyOrder = new LimitOrders(true, "product1", 10, BigDecimal.valueOf(100));
        orderAgent.addLimitOrder(buyOrder);

        orderAgent.executeOrders("product1", BigDecimal.valueOf(150));

        verify(executionService, never()).buy(anyString(), anyInt());
    }

    @Test
    public void testNoExecutionForSellOrder_WhenPriceIsLower() throws Exception {
        LimitOrders sellOrder = new LimitOrders(false, "product1", 10, BigDecimal.valueOf(100));
        orderAgent.addLimitOrder(sellOrder);

        orderAgent.executeOrders("product1", BigDecimal.valueOf(50));

        verify(executionService, never()).sell(anyString(), anyInt());
    }
}
