package org.afob.limit;


import org.afob.execution.ExecutionClient;
import org.junit.Assert;
import org.junit.Test;
import org.afob.limit.LimitOrderAgent;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ManageOrdersTest {
    @Test
    public void addOrdersTest(){
        String productId="IBM";
        boolean isBuy=false;
        BigDecimal amount=new BigDecimal(50);
        BigDecimal limit=new BigDecimal(100);

         addOrders( isBuy,productId,amount,limit);


        }

    @Test
    public void addOrdersTestException(){
        String productId=null;
        boolean isBuy=false;
        BigDecimal amount=new BigDecimal(50);
        BigDecimal limit=new BigDecimal(100);

        Exception exception = assertThrows(ExecutionClient.ExecutionException.class, () -> {
            addOrders( isBuy,productId,amount,limit);
        });

        assertEquals("failed to buy: environment error", exception.getMessage());
    }

}
