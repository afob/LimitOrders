package org.afob.limit;

import java.math.BigDecimal;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;

public class Order extends LimitOrderAgent {

	private ExecutionClient ec;

	public Order(ExecutionClient ec) {
		super(ec);
		this.ec = ec;
	}

	
	public void addOrder(String buyOrSell, String productId, int price, int limit) throws ExecutionException {
		
			try {
				if(buyOrSell.equalsIgnoreCase("B"))
				{
					if(limit>=price) {
					System.out.println("Buying "+productId+" @ $"+price);
					ec.buy(productId, price);}
				}
				else if(buyOrSell.equalsIgnoreCase("S"))
				{
					if(limit<=price) {
					System.out.println("Selling "+productId+" @ $"+price);
					ec.sell(productId, price);}
				}

			} catch (ExecutionException e) {
				throw new ExecutionException("Expected exception occured");
			}
	}
	
	 public static void main(String[] args) throws ExecutionException {
		 ExecutionClient ec = new ExecutionClient();
		 Order order = new Order(ec);

		order.priceTick("IBM", new BigDecimal(90));
		order.priceTick("IBM", new BigDecimal(110));

		order.addOrder("B","product1",150, 100);
		order.addOrder("B","product2",50, 100);

		order.addOrder("S","product3",150, 100);
		order.addOrder("S","product4",50, 100);
	    }


}
