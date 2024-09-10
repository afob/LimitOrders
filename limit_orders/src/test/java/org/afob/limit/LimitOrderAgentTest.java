package org.afob.limit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;

import org.afob.execution.ExecutionClient;
import org.afob.execution.ExecutionClient.ExecutionException;
import org.junit.Before;
import org.junit.Test;

public class LimitOrderAgentTest {

	private LimitOrderAgent agent;
	private MockExecutionClient executionClient;

	@Before
	public void setUp() {
		executionClient = new MockExecutionClient();
		agent = new LimitOrderAgent(executionClient);
	}

	@Test
	public void testBuyOrderExecuted() {
		agent.addOrder(true, "IBM", 1000, new BigDecimal("100"));
		agent.priceTick("IBM", new BigDecimal("99"));
		assertEquals(1000, executionClient.getBoughtAmount("IBM"));
	}

	@Test
	public void testSellOrderExecuted() {
		agent.addOrder(false, "IBM", 500, new BigDecimal("150"));
		agent.priceTick("IBM", new BigDecimal("151"));
		assertEquals(500, executionClient.getSoldAmount("IBM"));
	}

	@Test
	public void testOrderNotExecuted() {
		agent.addOrder(true, "IBM", 1000, new BigDecimal("100"));
		agent.priceTick("IBM", new BigDecimal("101"));
		assertEquals(0, executionClient.getBoughtAmount("IBM"));
	}

	@Test
	public void testExecutionException() {
		executionClient.setShouldFail(true);
		agent.addOrder(true, "IBM", 1000, new BigDecimal("100"));
		assertThrows(ExecutionException.class, () -> agent.priceTick("IBM", new BigDecimal("99")));
	}

	private static class MockExecutionClient extends ExecutionClient {
		private boolean shouldFail = false;
		private int boughtAmount = 0;
		private int soldAmount = 0;

		public void buy(String productId, int amount) throws ExecutionException {
			if (shouldFail) {
				throw new ExecutionException("Mock failure");
			}
			boughtAmount += amount;
		}

		public void sell(String productId, int amount) throws ExecutionException {
			if (shouldFail) {
				throw new ExecutionException("Mock failure");
			}
			soldAmount += amount;
		}

		public int getBoughtAmount(String productId) {
			return boughtAmount;
		}

		public int getSoldAmount(String productId) {
			return soldAmount;
		}

		public void setShouldFail(boolean shouldFail) {
			this.shouldFail = shouldFail;
		}
	}
}
