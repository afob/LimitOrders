package org.afob.limit.model;

import org.afob.limit.common.OrderType;

import java.math.BigDecimal;

public class Order {
    private final String orderId;
    private final OrderType type;
    private final String productId;
    private int amount;
    private BigDecimal limitPrice;
    private final long timestamp;
    private final long expirationTime;
    private final int clientPriority;

    private Order(Builder builder) {
        this.orderId = builder.orderId;
        this.type = builder.type;
        this.productId = builder.productId;
        this.amount = builder.amount;
        this.limitPrice = builder.limitPrice;
        this.timestamp = builder.timestamp;
        this.expirationTime = builder.expirationTime;
        this.clientPriority = builder.clientPriority;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderType getType() {
        return type;
    }

    public String getProductId() {
        return productId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(BigDecimal limitPrice) {
        this.limitPrice = limitPrice;
    }

    public int getClientPriority() {
        return clientPriority;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > timestamp + expirationTime;
    }

    public static class Builder {
        private String orderId;
        private OrderType type;
        private String productId;
        private int amount;
        private BigDecimal limitPrice;
        private long timestamp;
        private long expirationTime;
        public int clientPriority;

        public static Builder newInstance()
        {
            return new Builder();
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder type(OrderType type) {
            this.type = type;
            return this;
        }

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Builder limitPrice(BigDecimal limitPrice) {
            this.limitPrice = limitPrice;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder expirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder clientPriority(int clientPriority) {
            this.clientPriority = clientPriority;
            return this;
        }

        public Order build() {
            this.timestamp = System.currentTimeMillis();
            return new Order(this);
        }
    }
}