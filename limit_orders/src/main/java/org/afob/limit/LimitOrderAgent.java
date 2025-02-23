package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.limit.model.Order;
import org.afob.limit.common.OrderType;
import org.afob.limit.service.Persistence;
import org.afob.prices.PriceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class LimitOrderAgent implements PriceListener {

    private final Logger logger = LoggerFactory.getLogger(LimitOrderAgent.class);
    private final ExecutionClient executionClient;
    private final ConcurrentHashMap<String, PendingOrdersBook> productToOrdersMap;

    private final ExecutorService executor;
    private final ExecutorService cachePersistenceExecutor;

    private final Persistence cachePersistenceService;

    public LimitOrderAgent(ExecutionClient ec, Persistence CachePersistence) {
        this.executionClient = ec;
        this.productToOrdersMap = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.cachePersistenceExecutor = Executors.newSingleThreadExecutor();
        cachePersistenceService = CachePersistence;
    }

    /**
     * Rebuilds the cache from the persistence layer if the application is restarted.
     */
    public void rebuildCache() {
        List<Order> ordersFromCache = cachePersistenceService.getAllOrders();
        for (Order order : ordersFromCache) {
            addOrder(order.getType() == OrderType.BUY, order.getProductId(),
                    BigDecimal.valueOf(order.getAmount()),
                    order.getLimitPrice(), false);
        }
    }

    public void addOrder(boolean isBuy, String productId, BigDecimal amount, BigDecimal limitPrice, boolean pushToCache) {
        Order order = Order.Builder.newInstance()
                .orderId(UUID.randomUUID().toString())
                .type(isBuy ? OrderType.BUY : OrderType.SELL)
                .productId(productId)
                .amount(amount.intValue())
                .limitPrice(limitPrice)
                .timestamp(System.currentTimeMillis())
                .expirationTime(System.currentTimeMillis() + 1000)
                .build();

        PendingOrdersBook orders = productToOrdersMap.computeIfAbsent(productId, k -> new PendingOrdersBook());
        var ordersQueue = isBuy ? orders.pendingBuyOrders : orders.pendingSellOrders;
        ordersQueue.computeIfAbsent(limitPrice, k -> new ConcurrentLinkedQueue<>()).add(order);

        if(pushToCache) {
            cachePersistenceExecutor.submit(() -> cachePersistenceService.persist(order));
        }
    }

    @Override
    public void priceTick(String productId, BigDecimal price) {
        executor.submit(() -> {
            List<Order> buyOrders = getOrders(productId, price, true);
            List<Order> sellOrders = getOrders(productId, price, false);

            CompletableFuture<Void> buyOrderTask = CompletableFuture.runAsync(
                    () -> processOrders(buyOrders, (buyProduct, amount) -> {
                        try {
                            executionClient.buy(buyProduct, amount);
                        } catch (ExecutionClient.ExecutionException e) {
                            logger.error("Failed to execute buy order for {}", buyProduct, e);
                        }
                    }), executor
            );

            CompletableFuture<Void> sellOrderTask = CompletableFuture.runAsync(
                    () -> processOrders(sellOrders, (productId1, amount) -> {
                        try {
                            executionClient.sell(productId1, amount);
                        } catch (ExecutionClient.ExecutionException e) {
                            logger.error("Failed to execute sell order for {}", productId1, e);
                        }
                    }), executor
            );

            CompletableFuture.allOf(buyOrderTask, sellOrderTask).join();

            //we would want to publish the orders to kafka for update to Redis and DB
        });
    }

    private void processOrders(List<Order> orders, BiConsumer<String, Integer> orderExecutor) {
        if (orders.isEmpty()) {
            return;
        }

        List<Callable<Void>> tasks = new ArrayList<>();

        for (Order order : orders) {
            tasks.add(() -> {
                orderExecutor.accept(order.getProductId(), order.getAmount());
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Order processing interrupted", e);
        }
    }

    public List<Order> getOrders(String productId, BigDecimal marketPrice, boolean isBuy) {
        PendingOrdersBook orders = productToOrdersMap.get(productId);
        if (orders == null) {
            return Collections.emptyList();
        }

        if(isBuy) {
            return filterOrders(orders.pendingBuyOrders.headMap(marketPrice, true));
        } else {
            return filterOrders(orders.pendingSellOrders.headMap(marketPrice, true));
        }
    }

    private List<Order> filterOrders(NavigableMap<BigDecimal, Queue<Order>> matchedOrders) {
        return  matchedOrders
                    .values()
                    .stream()
                    .filter(queue -> !queue.isEmpty())
                    .map(Queue::poll)
                    .collect(Collectors.toList());
    }

    public boolean cancelOrder(String orderId, String productId) {
        PendingOrdersBook orders = productToOrdersMap.get(productId);
        if (orders == null) {
            return false;
        }

        return cancelOrderFromQueue(orders.pendingBuyOrders, orderId) ||
                cancelOrderFromQueue(orders.pendingSellOrders, orderId);
    }

    private boolean cancelOrderFromQueue(ConcurrentSkipListMap<BigDecimal, Queue<Order>> orderMap, String orderId) {
        for (Queue<Order> queue : orderMap.values()) {
            Iterator<Order> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Order order = iterator.next();
                if (order.getOrderId().equals(orderId)) {
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean updateOrder(String orderId, String productId, BigDecimal newAmount, BigDecimal newLimitPrice) {
        PendingOrdersBook orders = productToOrdersMap.get(productId);
        if (orders == null) {
            return false;
        }

        return updateOrderInQueue(orders.pendingBuyOrders, orderId, newAmount, newLimitPrice) ||
                updateOrderInQueue(orders.pendingSellOrders, orderId, newAmount, newLimitPrice);
    }

    private boolean updateOrderInQueue(ConcurrentSkipListMap<BigDecimal, Queue<Order>> orderMap, String orderId, BigDecimal newAmount, BigDecimal newLimitPrice) {
        for (Queue<Order> queue : orderMap.values()) {
            for (Order order : queue) {
                if (order.getOrderId().equals(orderId)) {
                    order.setAmount(newAmount.intValue());
                    order.setLimitPrice(newLimitPrice);
                    return true;
                }
            }
        }
        return false;
    }

    private void expireOrders() {
        for (PendingOrdersBook orders : productToOrdersMap.values()) {
            expireOrdersInQueue(orders.pendingBuyOrders);
            expireOrdersInQueue(orders.pendingSellOrders);
        }
    }

    private void expireOrdersInQueue(ConcurrentSkipListMap<BigDecimal, Queue<Order>> orderMap) {
        for (Queue<Order> queue : orderMap.values()) {
            queue.removeIf(Order::isExpired);
        }
    }

    public ConcurrentHashMap<String, PendingOrdersBook> getProductToOrdersMap() {
        return productToOrdersMap;
    }

    static class PendingOrdersBook {
        final ConcurrentSkipListMap<BigDecimal, Queue<Order>> pendingBuyOrders = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        final ConcurrentSkipListMap<BigDecimal, Queue<Order>> pendingSellOrders = new ConcurrentSkipListMap<>();
    }
}