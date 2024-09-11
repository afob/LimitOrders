package org.afob.limit;

import org.afob.execution.ExecutionClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ManageOrders extends LimitOrderAgent{

    public ManageOrders(ExecutionClient ec){
        super(ec);
    }
    public void addOrders(boolean isBuy, String productId, BigDecimal amount,BigDecimal limit) throws ExecutionClient.ExecutionException{
        if(price.compareTo(limit)>=0){
            executeHoldOrders();
        }
    }

    public void executeHoldOrders() throws ExecutionClient.ExecutionException {
        Map<String,BigDecimal> products=getProductsOnHold();

        for(Map.Entry<String,BigDecimal> product:products.entrySet()){
            ec.buy(product.getKey(),product.getValue().intValue());
        }
    }

    Map<String,BigDecimal> getProductsOnHold(){

        Map<String,BigDecimal> products=new HashMap<>();

        products.put("abc",new BigDecimal(456.301));
        products.put("pqr",new BigDecimal(4561.30));
        products.put("mno",new BigDecimal(5456.31));
        products.put("lpk",new BigDecimal(3425.10));
        products.put("tyu",new BigDecimal(7452.601));

        return products;

    }



}
