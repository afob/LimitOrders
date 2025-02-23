package org.afob.limit.service;

import org.afob.limit.common.OrderType;
import org.afob.limit.model.Order;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CachePersistenceService implements Persistence {
    private final JedisPool jedisPool = new JedisPool("localhost", 6379);

    private final int expiryTime = 12 * 60 * 60; // 12 hours

    @Override
    public void persist(Order order) {
        try (Jedis jedis = jedisPool.getResource()) {
            String orderKey = getOrderKey(order);
            String zsetKey = getCacheKey(order);

            // Store order details in Redis
            Map<String, String> orderDetails = Map.of(
                    "side", order.getType().toString(),
                    "quantity", String.valueOf(order.getAmount()),
                    "priority", String.valueOf(order.getClientPriority())
            );
            jedis.hset(orderKey, orderDetails);

            jedis.zadd(zsetKey, order.getClientPriority(), String.valueOf(order.getOrderId()));
            jedis.expire(zsetKey, expiryTime);
        }
    }

    private static String getOrderKey(Order order) {
        return "order:" + order.getOrderId();
    }

    private static String getCacheKey(Order order) {
        return "orders:" + order.getProductId() + ":" + order.getLimitPrice();
    }

    public void updateOrderAfterExecution(Order order, int executedAmount) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (order.getAmount() - executedAmount <= 0) {
                jedis.zrem(getCacheKey(order), order.getOrderId());
                jedis.del(getOrderKey(order));
            } else {
                jedis.hset(getOrderKey(order), "quantity", String.valueOf(order.getAmount() - executedAmount));
                jedis.expire(getCacheKey(order), expiryTime);
            }
        }
    }

    public List<Order> getAllOrders(){
        List<Order> orders = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> orderKeys = jedis.keys("order:*");
            for (String orderKey : orderKeys) {
                Map<String, String> orderDetails = jedis.hgetAll(orderKey);
                String productId = orderDetails.get("productId");
                BigDecimal limitPrice = new BigDecimal(orderDetails.get("limitPrice"));
                OrderType type = OrderType.valueOf(orderDetails.get("side"));
                int amount = Integer.parseInt(orderDetails.get("quantity"));
                long timestamp = Long.parseLong(orderDetails.get("timestamp"));
                long expirationTime = Long.parseLong(orderDetails.get("expirationTime"));

                Order order = Order.Builder.newInstance()
                .orderId(orderKey.split(":")[1])
                .type(type)
                .productId(productId)
                .amount(amount)
                .limitPrice(limitPrice)
                .timestamp(timestamp)
                .expirationTime(expirationTime)
                .build();

                orders.add(order);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get orders from cache", e);
        }

        return orders;
    }
}
