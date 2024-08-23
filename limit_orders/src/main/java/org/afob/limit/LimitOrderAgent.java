package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;

public class LimitOrderAgent implements PriceListener {

    private final ExecutionClient m_executionClient ;
    public LimitOrderAgent(final ExecutionClient ec) {
        m_executionClient = ec;
    }

    List<Order> cacheOrders = new ArrayList<>();

    @Override
    public void priceTick(String productId, BigDecimal price) {
       
        if (cacheOrders.size() < 1) {
            return;
        }
        List<Order> executedOrders = new ArrayList<>();
        for (Order order : cacheOrders) { 
            if( transactionType == TransactionType.BUY) {
                if ( price <= limit) {
                   m_executionClient.buy(productId, amount);
                } 
            } else if (transactionType == TransactionType.SELL) {
                m_executionClient.sell(productId, amount);
            }  
            executedOrders.add(order);
        } 
        cacheOrders.removeAll(executedOrders);
    }

    public void addOrder(TransactionType transactionType, String productId, 
                        int  amount, int limit) {

        cacheOrders.add(new Order(transactionType, productId, amount, limit));
        
    }

    private static class Order {
        TransactionType transactionType;
        String productId;
        int amount;
        int limit;

        Order(TransactionType transactionType, String productId, 
                        int  amount, int limit) {
             this.transactionType = transactionType;
             this.productId = productId;
             this.amount = amount;
             this.limit = limit;               
        }

        public void setTransactionType(TransactionType ty){
            this.transactionType = ty;
        }
        public void setProductId(int id){
            this.productId = id;
        }
        public void setAmount(int amount){
            this.amount = amount;
        }
        public void setLimit(int limit){
            this.limit = limit;
        }

        public TransactionType getTransactionType(){
            return transactionType;
        }

        public String getProductId(){
            return productId;
        }

        public int getAmount(){
            return amount;
        }
        public int getLimit(){
            return limit;
        }
    }
}


public enum TransactionType {
    BUY,
    SELL;

}
