package org.afob.prices;

import org.afob.execution.ExecutionClient;

import java.math.BigDecimal;

public interface PriceListener {

    /**
     *
     * @param productId the id of the product for which the price is given
     * @param price current price of the product
     */
    void priceTick(String productId, BigDecimal price) throws ExecutionClient.ExecutionException;

}
