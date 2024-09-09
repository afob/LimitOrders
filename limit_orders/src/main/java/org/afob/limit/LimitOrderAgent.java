package org.afob.limit;

import org.afob.prices.PriceListener;

import java.math.BigDecimal;

public class LimitOrderAgent implements PriceListener {
    private final OrderAgent orderAgent;

    public LimitOrderAgent(OrderAgent orderAgent) {
        this.orderAgent = orderAgent;
    }

    /**
     * Adds a new limit order to the OrderManager.
     */
    public void addLimitOrder(boolean isBuyOrder, String productId, int quantity, BigDecimal limitPrice) {
        LimitOrders limitOrders = new LimitOrders(isBuyOrder, productId, quantity, limitPrice);
        orderAgent.addLimitOrder(limitOrders);
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
        orderAgent.executeOrders(productId, price);
    }
}


