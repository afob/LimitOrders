package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class LimitOrderAgent implements PriceListener {
private final ExecutionClient executionClient;
public List<Order> orders; 
Order ibmOrder=new Order(Order.OrderType.PURCHASE,"12ibm",1000,new BigDecimal(100));

    public LimitOrderAgent(final ExecutionClient ec) {
    	this.executionClient=ec;
    	this.orders=new ArrayList<Order>();
    	orders.add(ibmOrder);
    	
    }
    
    public void addOrder(Order.OrderType orderType, String productId, int amount, BigDecimal limitPrice) {
        Order order = new Order(orderType, productId, amount, limitPrice);
        orders.add(order);
      }

    @Override
    public void priceTick(String productId, BigDecimal price) {
    	Iterator<Order> iterator=orders.iterator();
    	while(iterator.hasNext()) {
    		for(Order order: orders) {
    			if(order.getProductId().equals(productId)) {
    				if((order.getType().equals(Order.OrderType.PURCHASE)&& price.compareTo(order.getLimit())==-1)||(order.getType().equals(Order.OrderType.SELL)&& price.compareTo(order.getLimit())==1)) {
    					try {
							performOrder(order);
						} catch (ExecutionException e) {
							e.printStackTrace();
						}
    					iterator.remove();
    				}
    			}
    		}
    	}
    }


	private void performOrder(Order order) throws ExecutionException {
		// TODO Auto-generated method stub
		if(order.getType().equals(Order.OrderType.PURCHASE)) {
			executionClient.buy(order.getProductId(), order.getAmount());
		}else if(order.getType().equals(Order.OrderType.SELL)) {
			executionClient.sell(order.getProductId(), order.getAmount());
		}
	}

}
