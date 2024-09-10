package org.afob.limit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;

public class LimitOrderAgent implements PriceListener {

	private final ExecutionClient executionClient;
	private final List<Order> orders = new ArrayList<>();

	public LimitOrderAgent(final ExecutionClient ec) {
		this.executionClient = ec;
	}

	@Override
	public void priceTick(String productId, BigDecimal price) {
		List<Order> executedOrders = new ArrayList<>();
		for (Order order : orders) {
			if (order.productId.equals(productId) && 
					((order.isBuy && price.compareTo(order.limit) <= 0) || 
							(!order.isBuy && price.compareTo(order.limit) >= 0))) {
				try {
					if (order.isBuy) {
						executionClient.buy(order.productId, order.amount);
					} else {
						executionClient.sell(order.productId, order.amount);
					}
					executedOrders.add(order);
				} catch (ExecutionException e) {
					System.err.println("Order execution failed: " + e.getMessage());
				}
			}
		}
		orders.removeAll(executedOrders);
	}

	public void addOrder(boolean isBuy, String productId, int amount, BigDecimal limit) {
		orders.add(new Order(isBuy, productId, amount, limit));
	}
	
	@SuppressWarnings("unused")
	private static class Order {
		boolean isBuy;
		String productId;
		int amount;
		BigDecimal limit;

		public boolean isBuy() {
			return isBuy;
		}

		public void setBuy(boolean isBuy) {
			this.isBuy = isBuy;
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

		public BigDecimal getLimit() {
			return limit;
		}

		public void setLimit(BigDecimal limit) {
			this.limit = limit;
		}

		Order(boolean isBuy, String productId, int amount, BigDecimal limit) {
			this.isBuy = isBuy;
			this.productId = productId;
			this.amount = amount;
			this.limit = limit;
		}
	}

	public static void main(String[] args) {
		ExecutionClient executionClient = new ExecutionClient() {
			
			public void buy(String productId, int amount) throws ExecutionException {
				System.out.println("Buying " + amount + " shares of " + productId);
			}

			public void sell(String productId, int amount) throws ExecutionException {
				System.out.println("Selling " + amount + " shares of " + productId);
			}
		};

		LimitOrderAgent agent = new LimitOrderAgent(executionClient);

		// Add an order to buy 1000 shares of IBM when the price drops below $100
		agent.addOrder(true, "IBM", 1000, new BigDecimal("100"));

		// Simulate price ticks
		agent.priceTick("IBM", new BigDecimal("105")); // No action
		agent.priceTick("IBM", new BigDecimal("99"));  // Should trigger buy order
	}

}
