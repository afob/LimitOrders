# LimitOrders

The task is the implement a simple limit order system utilising the provided framework. 
You can assume that in a live environment your LimitOrderAGent class would be provided 
with market data via the priceTick method and would be able to execute orders via the ExecutionClient function.

## The trading_framework model
The **trading_framework** module is provided and cannot be modified.
It has some limitations and flaws, feel free to point them out as you implement your solution.

## Guidance
### In the **limit_orders** module only:
1. Implement _org.afob.limit.LimitOrderAgent_ such that it buys 1000 shares of IBM when the price drops below $100

2. Extend LimitOrderAgent such that:
   * it accepts orders through an addOrder method, that accepts the following parameters
     * a flag indicating whether to buy or sell
     * product id
     * amount to buy/sell
     * the limit at which to buy or sell  
   * it executes any held orders when the market price is at or better than the limit 

##Solution
### Key Considerations for the Solution
1. Multiple orders can be added concurrently
2. Orders can be modified or cancelled
3. There could be thousands of products and millions of orders
4. Orders should not be lost if there are technical issues in the system

### Design
1. **OrderBook**: A class to store all the orders. It will have a map of product id to a list of orders for that product.
2. **Order**: A class to represent an order. It will have the following fields:
   * product id
   * amount
   * limit
   * buy/sell flag
   * order id
3. **LimitOrderAgent**: A class to implement the following methods:
   * addOrder: To add an order to the order book
   * modifyOrder: To modify an existing order
   * cancelOrder: To cancel an existing order
   * executeOrders: To execute orders when the market price is at or better than the limit
4. ConcurrentHashMap to store orders against each product id. This will allow multiple threads to add, modify, or cancel orders concurrently.
5. ConcurrentLinkedQueue to help maintain the order in sequence and provides mechanism to fetch order "<=" or ">=" market price of the product
6. The filtered buy and sell orders are executed concurrently.
7. The orders are pushed to Redis for external persistence and recovery in case of system failure.

### Further improvements
1. The data structure holding orders can be limited to a certain size to prevent memory issues.
   A sliding window mechanism can be implemented to remove old orders and load them from Redis as per market price movement.
2. A further improvement could be to partition the distribution of orders based on the product.
   For example: The concurrent hash map can have 10 entries for IBM based on the opening price. 
   Say, IBM opens 100 then roughly by calculating the upper circuit and lower circuit we can distribute the orders in concurrent hash map in 10 buckets.
3. The system can be made more fault-tolerant by implementing a backup mechanism to store orders in a secondary database.
3. Orders for priority clients can be given preference by holding them in a separate queue.