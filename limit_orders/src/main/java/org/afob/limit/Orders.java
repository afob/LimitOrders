package org.afob.limit;

import java.math.BigDecimal;

private static class Orders {
    private final boolean isBuy;
    private final String productId;
    private final int quantity;
    private final BigDecimal limitPrice;

    public Order(boolean isBuy, String productId, int quantity, BigDecimal limitPrice) {
        this.isBuy = isBuy;
        this.productId = productId;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
    }

    public boolean isBuy() {
        return isBuy;
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

    @Override
    public String toString() {
        return "Order{" +
                "isBuy=" + isBuy +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", limitPrice=" + limitPrice +
                '}';
    }
}
}
