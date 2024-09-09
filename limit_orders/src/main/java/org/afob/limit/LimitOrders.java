package org.afob.limit;

import java.math.BigDecimal;

/**
 * Represents a limit order.
 */
public class LimitOrders {
    private final boolean isBuyOrder;
    private final String productId;
    private final int quantity;
    private final BigDecimal limitPrice;

    public LimitOrders(boolean isBuyOrder, String productId, int quantity, BigDecimal limitPrice) {
        this.isBuyOrder = isBuyOrder;
        this.productId = productId;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
    }

    public boolean isBuyOrder() {
        return isBuyOrder;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }
}
