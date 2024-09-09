package org.afob.limit;

import org.afob.execution.ExecutionClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OrderAgent {
    private final ExecutionService executionService;
    private final List<LimitOrders> limitOrders = new ArrayList<>();

    public OrderAgent(ExecutionService executionService) {
        this.executionService = executionService;
    }

    public void addLimitOrder(LimitOrders limitOrders) {
        this.limitOrders.add(limitOrders);
    }

    public void executeOrders(String productId, BigDecimal price) {
        Iterator<LimitOrders> iterator = limitOrders.iterator();

        while (iterator.hasNext()) {
            LimitOrders limitOrders = iterator.next();

            if (limitOrders.getProductId().equals(productId)) {
                try {
                    if (limitOrders.isBuyOrder() && price.compareTo(limitOrders.getLimitPrice()) <= 0) {
                        executionService.buy(productId, limitOrders.getQuantity());
                        iterator.remove(); // Remove order after execution
                    } else if (!limitOrders.isBuyOrder() && price.compareTo(limitOrders.getLimitPrice()) >= 0) {
                        executionService.sell(productId, limitOrders.getQuantity());
                        iterator.remove(); // Remove order after execution
                    }
                } catch (ExecutionClient.ExecutionException e) {
                    System.err.println("Order execution failed: " + e.getMessage());
                }
            }
        }
    }
}
