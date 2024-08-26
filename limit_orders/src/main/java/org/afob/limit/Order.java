package org.afob.limit;

import java.math.BigDecimal;

public class Order {
    private String productId;
    private OrderType type;
    private BigDecimal price;
    private int quantity;

    public Order(String productId, OrderType type, BigDecimal price, int quantity) {
        this.productId = productId;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public OrderType getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

enum OrderType {
    BUY, SELL
}