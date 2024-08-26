package org.afob.limit;

import java.math.BigDecimal;

public class Order {
	public enum OrderType{	
		PURCHASE,SELL
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
	public BigDecimal getLimit() {
		return limit;
	}
	public Order(OrderType type, String productId, int amount, BigDecimal limit) {
		super();
		this.type = type;
		this.productId = productId;
		this.amount = amount;
		this.limit = limit;
	}
	public void setLimit(BigDecimal limit) {
		this.limit = limit;
	}
	OrderType type;
	String productId;
	int amount;
	BigDecimal limit;
	@Override
	public String toString() {
		return "Order [type=" + type + ", productId=" + productId + ", amount=" + amount + ", limit=" + limit + "]";
	}




}
