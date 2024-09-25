package org.afob.limit;

import java.math.BigDecimal;

public class Order {

    public enum Type { BUY, SELL }
    
    private Type type;
    private String productId;
    private int amount;
    private BigDecimal limitPrice;

    public Order(Type type, String productId, int amount, BigDecimal limitPrice) {
        this.type = type;
        this.productId = productId;
        this.amount = amount;
        this.limitPrice = limitPrice;
    }

    public Type getType() {
        return type;
    }

    public String getProductId() {
        return productId;
    }

    public int getAmount() {
        return amount;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    @Override
    public String toString() {
        return "Order{" +
                "type=" + type +
                ", productId='" + productId + '\'' +
                ", amount=" + amount +
                ", limitPrice=" + limitPrice +
                '}';
    }
}
