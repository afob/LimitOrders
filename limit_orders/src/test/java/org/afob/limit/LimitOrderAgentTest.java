package org.afob.limit;

import org.afob.prices.PriceListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LimitOrderAgentTest {

    private LimitOrderAgent limitOrderAgent;
    private OrderAgent orderAgent;

    @BeforeEach
    void setUp() {
        // Mock the OrderAgent dependency
        orderAgent = mock(OrderAgent.class);
        limitOrderAgent = new LimitOrderAgent(orderAgent);
    }

    @Test
    void testAddLimitOrder() {
        // Define input parameters
        boolean isBuyOrder = true;
        String productId = "PROD123";
        int quantity = 100;
        BigDecimal limitPrice = new BigDecimal("150.00");

        // Call the method under test
        limitOrderAgent.addLimitOrder(isBuyOrder, productId, quantity, limitPrice);

        // Capture the argument passed to addLimitOrder in orderAgent
        ArgumentCaptor<LimitOrders> argumentCaptor = ArgumentCaptor.forClass(LimitOrders.class);
        verify(orderAgent).addLimitOrder(argumentCaptor.capture());

        // Verify the captured LimitOrders object
        LimitOrders capturedOrder = argumentCaptor.getValue();
        assertEquals(isBuyOrder, capturedOrder.isBuyOrder());
        assertEquals(productId, capturedOrder.getProductId());
        assertEquals(quantity, capturedOrder.getQuantity());
        assertEquals(limitPrice, capturedOrder.getLimitPrice());
    }

    @Test
    void testPriceTick() {
        // Define input parameters
        String productId = "PROD123";
        BigDecimal price = new BigDecimal("200.00");

        // Call the method under test
        limitOrderAgent.priceTick(productId, price);

        // Verify that executeOrders was called on orderAgent
        verify(orderAgent, times(1)).executeOrders(productId, price);
    }
}
