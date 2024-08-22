package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;

public class LimitOrderAgent implements PriceListener {

  private List<LimitOrder> orders;

  private final ExecutionClient executionClient;

  public LimitOrderAgent(final ExecutionClient executionClient) {
    this.executionClient = executionClient;
    this.orders = new ArrayList<>();
  }

  public void addOrder(LimitOrder.OrderType orderType, String productId, int amount, double limitPrice) {
    LimitOrder order = new LimitOrder(orderType, productId, amount, limitPrice);
    orders.add(order);
  }

  @Override
  public void priceTick(String productId, BigDecimal price) {

    Iterator<LimitOrder> iterator = orders.iterator();

    while (iterator.hasNext()) {
      LimitOrder order = iterator.next();

      if (order.getProductId().equals(productId)) {
        if ((order.getType() == LimitOrder.OrderType.PURCHASE && price <= order.getLimitPrice())
            || (order.getType() == LimitOrder.OrderType.SELL && price >= order.getLimitPrice())) {
          executeOrder(order);
          iterator.remove();
        }
      }
    }
  }

  private void executeOrder(LimitOrder order) {
    if (order.getType() == LimitOrder.OrderType.PURCHASE) {
      executionClient.buy(order.getProductId(), order.getAmount(), order.getLimitPrice());
    } else if (order.getType() == LimitOrder.OrderType.SELL) {
      executionClient.sell(order.getProductId(), order.getAmount(), order.getLimitPrice());
    }
  }

  private static class LimitOrder {

    public enum OrderType {
      PURCHASE, SELL
    }

    private final OrderType type;

    private final String productId;

    private final int amount;

    private final double limit;

    public LimitOrder(OrderType type, String productId, int amount, double limitPrice) {
      this.type = type;
      this.productId = productId;
      this.amount = amount;
      this.limit = limitPrice;
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

    public double getLimitPrice() {
      return limit;
    }
  }
}
