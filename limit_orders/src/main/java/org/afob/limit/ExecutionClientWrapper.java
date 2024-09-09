package org.afob.limit;

import org.afob.execution.ExecutionClient;


public class ExecutionClientWrapper implements ExecutionService {
    private final ExecutionClient executionClient;

    public ExecutionClientWrapper(ExecutionClient executionClient) {
        this.executionClient = executionClient;
    }

    @Override
    public void buy(String productId, int amount) throws ExecutionClient.ExecutionException {
        executionClient.buy(productId, amount);
    }

    @Override
    public void sell(String productId, int amount) throws ExecutionClient.ExecutionException {
        executionClient.sell(productId, amount);
    }

    @Override
    public void execute(String action, String productId, int amount) {
        executionClient.execute(action, productId, amount);
    }
}