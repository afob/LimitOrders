package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LimitOrderAgent implements PriceListener {

	private final ExecutionClient ec;
    public LimitOrderAgent(final ExecutionClient ec) {
    	this.ec = ec;
    }

    private static class Order {
        
        private OrderType type;
        String productId;
        int amount;
        BigDecimal price;

        Order(OrderType type, String productId, int amount, BigDecimal  price) {
            this.type = type;
            this.productId = productId;
            this.amount = amount;
            this.price = price;
        }
        


		public OrderType getType() {
			return type;
		}

		public void setType(OrderType type) {
			this.type = type;
		}

		public String getProductId() {
			return productId;
		}

		public void setProductId(String productId) {
			this.productId = productId;
		}

		public int getAmount() {
			return amount;
		}

		public void setAmount(int amount) {
			this.amount = amount;
		}

		public BigDecimal getPrice() {
			return price;
		}

		public void setLimitPrice(BigDecimal price) {
			this.price = price;
		}

		
    }
    enum OrderType {
        BUY, SELL
    }
    private final List<Order> orders = new ArrayList<>();
    public void addOrder(OrderType orderType, String productId, int amount, BigDecimal price) {
        orders.add(new Order(orderType, productId, amount, price));
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
    	List<Order> executedOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.productId.equals(productId)) {
                if ((order.type == OrderType.BUY && price.compareTo(order.price) <= 0) ||
                    (order.type == OrderType.SELL && price.compareTo(order.price) >= 0)) {
                    if (order.type == OrderType.BUY) {
                        try {
							ec.buy(productId, order.amount);
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
                    } else {
                        try {
							ec.sell(productId, order.amount);
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
                    }
                    executedOrders.add(order);
                }
            }
        }
        orders.removeAll(executedOrders);
    }

}
