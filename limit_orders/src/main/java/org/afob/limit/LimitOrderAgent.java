package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LimitOrderAgent implements PriceListener {

    private static class LimitOrder {
        private final boolean isBuy;
        private final String productId;
        private final int amount;
        private final BigDecimal limitPrice;

        private LimitOrder(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
            this.isBuy = isBuy;
            this.productId = productId;
            this.amount = amount;
            this.limitPrice = limitPrice;

        }

        public boolean isBuy() {
            return isBuy;
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
    }

    //Store orders in List
    private final List<LimitOrder> orders = new ArrayList<>();
    private final ExecutionClient executionClient;

    public LimitOrderAgent(final ExecutionClient ec) {
        this.executionClient = ec;
    }

    // Method to add orders (buy/sell) to the agent
    public void addOrder(boolean isBuy, String productId, int amount, BigDecimal limitPrice) {
        orders.add(new LimitOrder(isBuy, productId, amount, limitPrice));
    }

    @Override
    public void priceTick(String productId, BigDecimal currentPrice) throws ExecutionClient.ExecutionException {
        try {
            for (LimitOrder order : orders) {
                if (order.getProductId().equals(productId)) {
                    if (order.isBuy() && currentPrice.compareTo(order.getLimitPrice()) <= 0) {
                        executeOrder(order);
                    } else if (!order.isBuy() && currentPrice.compareTo(order.getLimitPrice()) >= 0) {
                        executeOrder(order);

                    }
                }
            }
        } catch (ExecutionClient.ExecutionException e) {
            // Handle any exception that occurs inside the priceTick method
            System.err.println("Error processing price tick for product: " + productId);
            e.printStackTrace();
        }
    }

    private void executeOrder(LimitOrder order) throws ExecutionClient.ExecutionException {
        try {
            if (order.isBuy()) {
                executionClient.buy(order.getProductId(), order.getAmount());
            } else {
                executionClient.sell(order.getProductId(), order.getAmount());
            }
        } catch (ExecutionClient.ExecutionException e) {
            System.err.println("Failed to execute order for product: " + order.getProductId() + " due to: " + e.getMessage());
        }
    }

}
