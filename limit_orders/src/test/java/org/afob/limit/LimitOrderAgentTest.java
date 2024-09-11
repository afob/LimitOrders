package org.afob.limit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

public class LimitOrderAgentTest{
	
	@Mock
	private ExecutionClient executionClient = new ExecutionClient();
	
	@Mock
	private LimitOrderAgent limitOrderAgent = new LimitOrderAgent(executionClient);
	
	@Mock
	private Order order = new Order(executionClient);
	
	/*
	 * @Test public void addTestsHere() { Assert.fail("not implemented"); }
	 */
    
    @Test
    public void priceTickTest() {
    	BigDecimal limit = new BigDecimal(50);
    	assertThrows("Expected exception occured in priceTick():",ExecutionException.class, () -> limitOrderAgent.priceTick("product1",limit));    
    	assertThrows("Unexpected exception occured in priceTick():",Exception.class, () -> limitOrderAgent.priceTick(null,null));    	

    }
    
    @Test
    public void addOrderTest() {
    	assertThrows("Expected exception occured in addOrder():",ExecutionException.class, () -> order.addOrder("B","product1",100, 100));
    	assertThrows("Unexpected exception occured in addOrder():",Exception.class, () -> order.addOrder(null,null,0, 0));    }

}