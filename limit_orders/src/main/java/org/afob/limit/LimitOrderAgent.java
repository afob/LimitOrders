package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;

public class LimitOrderAgent implements PriceListener {

	private ExecutionClient ec;
    public LimitOrderAgent(final ExecutionClient ec) {
    	this.ec = ec;
    }


    @Override
    public void priceTick(String productId, BigDecimal price) throws ExecutionException {
    	try {
    	BigDecimal limit = new BigDecimal(100);
    	if (limit.compareTo(price) == 1) { 
				System.out.println("Buying "+productId+" @ $"+price);
				ec.buy(productId, 1000);
			}
    	else if(limit.compareTo(price) == 0){
			System.out.println("Limit and price are equal");
    	}
    	else {
    		System.out.println("Selling "+productId+" @ $"+price);
			ec.sell(productId, 1000);
    	}
    	}
    	catch (ExecutionException e) {
			throw new ExecutionException("Expected error occured");
		}
    	} 

    }
