package org.afob.limit;

import java.math.BigDecimal;

public class LimitOrder {
    private boolean buy;
    private String productId;
    private int amount;
    private BigDecimal limitPrice;

    public LimitOrder(boolean buy, String productId, int amount, BigDecimal limitPrice) {
        this.buy = buy;
        this.productId = productId;
        this.amount = amount;
        this.limitPrice = limitPrice;
    }

    public boolean isBuy() {
        return buy;
    }

    public String getProductId() {
        return productId;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isExecutable(BigDecimal marketPrice) {
        return buy && marketPrice.compareTo(limitPrice) <=0 || !buy && marketPrice.compareTo(limitPrice) >=0;
    }
}
