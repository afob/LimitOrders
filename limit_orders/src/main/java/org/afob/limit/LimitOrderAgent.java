package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.prices.PriceListener;

import java.math.BigDecimal;

public class LimitOrderAgent implements PriceListener {

    public LimitOrderAgent(final ExecutionClient ec) {
    }


    @Override
    public void priceTick(String productId, BigDecimal price) {

    }

}
