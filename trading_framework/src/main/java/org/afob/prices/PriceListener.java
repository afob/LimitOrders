package org.afob.prices;

import java.math.BigDecimal;

import org.afob.execution.ExecutionClient.ExecutionException;

public interface PriceListener {

    /**
     *
     * @param productId the id of the product for which the price is given
     * @param price current price of the product
     * @throws ExecutionException 
     */
    void priceTick(String productId, BigDecimal price) throws ExecutionException;

}
