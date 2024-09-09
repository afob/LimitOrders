package org.afob.execution;

public final class ExecutionClient {

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

    public void execute(String action, String productId, int amount) {
        System.out.println("Executing " + action + " order for " + amount + " shares of " + productId);
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
