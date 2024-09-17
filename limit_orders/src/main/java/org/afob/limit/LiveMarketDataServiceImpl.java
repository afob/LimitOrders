package org.afob.limit;

import java.math.BigDecimal;

public class LiveMarketDataServiceImpl implements LiveMarketDataService{
    public BigDecimal getCurrentMarketPriceForProduct(String productId) throws Exception{
        //Here we have to call API/DB anything to get the Market price
        //Below is the fetched market price of IBM
        try {
            if (productId.equalsIgnoreCase("IBM")) {
                return new BigDecimal(98.02);
            }
            return new BigDecimal(1000.09);
        }catch(Exception e){
            e.printStackTrace();
        }
        throw new Exception("Something went wrong in fetching Market Data");
    }
}
