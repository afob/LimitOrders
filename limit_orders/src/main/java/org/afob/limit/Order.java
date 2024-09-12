package org.afob.limit;

import org.afob.execution.ExecutionClient;

public class Order {

    private final boolean buyOrSell; // true for buy, false for sell
    private final String productId;
    private final int amount;
    private final double limitPrice;

    public Order(boolean buyOrSell, String productId, int amount, double limitPrice) {
        this.buyOrSell = buyOrSell;
        this.productId = productId;
        this.amount = amount;
        this.limitPrice = limitPrice;
    }

    public boolean isBuy() {
        return buyOrSell;
    }

    public String getProductId() {
        return productId;
    }

    public int getAmount() {
        return amount;
    }

    public double getLimitPrice() {
        return limitPrice;
    }
}



   


