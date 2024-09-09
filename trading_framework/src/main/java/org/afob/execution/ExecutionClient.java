package org.afob.execution;

public class ExecutionClient {

    /**
     * Execute a buy order
     * @param productId - the product to buy
     * @param amount - the amount to buy
     * @throws ExecutionException
     */
    public void buy(String productId, int amount) throws ExecutionException {
        throw new ExecutionException("failed to buy: environment error");
    }

    /**
     * Execute a sell order
     * @param productId - the product to sell
     * @param amount - the amount to sell
     * @throws ExecutionException
     */
    public void sell(String productId, int amount) throws ExecutionException {
        throw new ExecutionException("failed to sell: environment error");
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
