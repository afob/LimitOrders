package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.junit.Assert;
import org.junit.Test;
import org.afob.limit.LimitOrderAgent;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class LimitOrderAgentTest {

    @Test
    public void addTestsHere() {
        Assert.fail("not implemented");
    }

    @Test
    public void priceTickTest(){
        String productId="IBM";
        BigDecimal price=new BigDecimal(100);
        priceTick( productId, price);

    }

    @Test
    public void priceTickTestException(){
        String productId=null;
        BigDecimal price=new BigDecimal(100);

        Exception exception = assertThrows(ExecutionClient.ExecutionException.class, () -> {
            priceTick( productId, price);
        });

        assertEquals("failed to buy: environment error", exception.getMessage());
    }
}
