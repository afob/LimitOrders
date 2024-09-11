package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LimitOrderAgent implements PriceListener {

    private final ExecutionClient executionClient;
    private final List<LimitOrder> limitOrders;

    public LimitOrderAgent(final ExecutionClient executionClient) {
        this.executionClient = executionClient;
        limitOrders = new ArrayList<>();
    }


    @Override
    public void priceTick(String productId, BigDecimal price) {
        List<LimitOrder> executedOrders = new ArrayList<>();
        for (LimitOrder order : limitOrders) {
            if (order.productId().equals(productId)) {
                boolean shouldExecuteOrder = false;

                if (order.limitOrderType() == LimitOrder.LimitOrderType.BUY && price.compareTo(order.limitAmount()) <= 0) {
                    shouldExecuteOrder = true;
                } else if (order.limitOrderType() == LimitOrder.LimitOrderType.SELL && price.compareTo(order.limitAmount()) >= 0) {
                    shouldExecuteOrder = true;
                }

                if (shouldExecuteOrder) {
                    try {
                        if (order.limitOrderType() == LimitOrder.LimitOrderType.BUY) {
                            executionClient.buy(order.productId(), order.quantity());
                        } else {
                            executionClient.sell(order.productId(), order.quantity());
                        }
                        executedOrders.add(order);
                    } catch (ExecutionClient.ExecutionException e) {
                        System.err.println("Order processing failed: " + e.getMessage());
                    }
                }
            }

        }
        limitOrders.removeAll(executedOrders);

    }

    public void addLimitOrder(final LimitOrder limitOrder) {
        limitOrders.add(limitOrder);
    }

    public record LimitOrder(LimitOrderAgent.LimitOrder.LimitOrderType limitOrderType, String productId, int quantity,
                             BigDecimal limitAmount) {
            public enum LimitOrderType {BUY, SELL}

    }
}
