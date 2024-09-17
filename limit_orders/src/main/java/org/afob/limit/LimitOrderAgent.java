package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class LimitOrderAgent implements PriceListener {
    private ExecutionClient executorClient;
    public LimitOrderAgent(final ExecutionClient executorClient) {
        this.executorClient= executorClient;
    }

    @Override
    public int priceTick(String productId, BigDecimal price) {
        try {
            LiveMarketDataService marketService = new LiveMarketDataServiceImpl();
            BigDecimal mktPrice = marketService.getCurrentMarketPriceForProduct(productId);
            if(mktPrice.compareTo(price) < 0){
                return 1;
            }
            return -1;
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;

    }

    public String executeOrder(Order order) {
        System.out.println("Executing order: " + order);

        try{
            ReentrantLock rl = new ReentrantLock();
            rl.lock();
            if(order.getBuyOrSell()){
                try {
                    executorClient.buy(order.getProductId(), order.getAmount());
                }catch(ExecutionClient.ExecutionException ex){
                    //Do nothing
                }
                rl.unlock();
                return "SUCCESSFULLY BOUGHT";
            }else{
                try {
                    executorClient.buy(order.getProductId(), order.getAmount());
                }catch(ExecutionClient.ExecutionException ex){
                    //Do nothing
                }
                rl.unlock();
                return "SUCCESSFULLY SOLD";
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }
        return "Something went wrong with order.\n" +order.toString();

    }

}
