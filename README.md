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


