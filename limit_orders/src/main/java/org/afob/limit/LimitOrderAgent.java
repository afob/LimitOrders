package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;

public class LimitOrderAgent implements PriceListener {

    String productId;
    BigDecimal price;

    ExecutionClient ec;

    public LimitOrderAgent(final ExecutionClient ec) {
        this.ec=ec;
    }

    public void limitOrder() throws ExecutionClient.ExecutionException {

        if(productId=="IBM"){
            if(this.price.compareTo(BigDecimal.valueOf(100))<0){
                ec.buy(productId,1000*this.price.intValue());
            }
        }
    }

    @Override
    public void priceTick(String productId, BigDecimal price)  {
        this.productId=productId;
        this.price=price;
        try {
            limitOrder();
        }catch (ExecutionClient.ExecutionException e){
            System.out.println("exception thrown"+e);
        }
    }

}
