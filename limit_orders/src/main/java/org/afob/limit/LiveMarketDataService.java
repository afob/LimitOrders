package org.afob.limit;

import java.math.BigDecimal;

public interface LiveMarketDataService {
    public BigDecimal getCurrentMarketPriceForProduct(String productId) throws Exception;
}
