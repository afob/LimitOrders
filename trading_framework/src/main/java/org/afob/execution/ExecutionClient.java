package org.afob.execution;

import java.util.logging.Logger;

public final class ExecutionClient {
    private static final Logger LOGGER = Logger.getLogger(ExecutionClient.class.getName());

    /**
     * Execute a buy order
     * @param productId - the product to buy
     * @param amount - the amount to buy
     * @throws ExecutionException
     */
    public void buy(String productId, int amount) throws ExecutionException {
        try {
            LOGGER.info("Buying " + amount + " units of " + productId);
        } catch (Exception e) {
            throw new ExecutionException("Failed to buy: " + e.getMessage());
        }
    }

    /**
     * Execute a sell order
     * @param productId - the product to sell
     * @param amount - the amount to sell
     * @throws ExecutionException
     */
    public void sell(String productId, int amount) throws ExecutionException {
        try {
            LOGGER.info("Selling " + amount + " units of " + productId);
        } catch (Exception e) {
            throw new ExecutionException("Failed to sell: " + e.getMessage());
        }
    }

    public static class ExecutionException extends Exception {
        public ExecutionException(String message) {
            super(message);
        }

        public ExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}