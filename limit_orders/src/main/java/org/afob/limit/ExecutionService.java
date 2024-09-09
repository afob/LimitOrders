package org.afob.limit;

import org.afob.execution.ExecutionClient;

public interface ExecutionService {
    void buy(String productId, int amount) throws ExecutionClient.ExecutionException;
    void sell(String productId, int amount) throws ExecutionClient.ExecutionException;
    void execute(String action, String productId, int amount);
}