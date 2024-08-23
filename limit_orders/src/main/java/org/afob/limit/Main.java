package org.afob.limit;

import java.math.BigDecimal;

import org.afob.execution.ExecutionClient;

public class Main {
    public static void main(String[] args) {
        ExecutionClient executionClient = new ExecutionClient();
        LimitOrderAgent limitOrderAgent = new LimitOrderAgent(executionClient);
        limitOrderAgent.addOrder(true, "IBM", 1000, BigDecimal.valueOf(100));
        limitOrderAgent.addOrder(false, "IBM", 500, BigDecimal.valueOf(120));
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(90)); // Should trigger the buy order
        limitOrderAgent.priceTick("IBM", BigDecimal.valueOf(130)); // Should trigger the sell order
    }
}
		