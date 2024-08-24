package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import org.afob.limit.Orders;

public class LimitOrderAgent implements PriceListener {

    private final ExecutionClient executionClient;
    private final List<Orders> orders;

    public LimitOrderAgent(final ExecutionClient ec) {
        this.executionClient = ec;
        this.orders = new ArrayList<>();
    }


    @Override
    public void priceTick(String productId, BigDecimal price) {

        orders.removeIf(order -> {
            if (order.getProductId().equals(productId)) {
                if ((order.isBuy() && price <= order.getLimitPrice()) ||
                        (!order.isBuy() && price >= order.getLimitPrice())) {
                    try {
                        if (order.isBuy()) {
                            executionClient.buy(order.getProductId(), order.getQuantity());
                        } else {
                            executionClient.sell(order.getProductId(), order.getQuantity());
                        }
                        return true; // Remove the order after execution
                    } catch (ExecutionClient.ExecutionException e) {
                        System.err.println("Failed to execute order: " + order + ": " + e.getMessage());
                    }
                }
            }
            return false; // Keep the order
        });
    }

    public void addOrder(boolean isBuy, String productId, int quantity, BigDecimal limitPrice) {
        orders.add(new Orders(isBuy, productId, quantity, limitPrice));
    }

}
