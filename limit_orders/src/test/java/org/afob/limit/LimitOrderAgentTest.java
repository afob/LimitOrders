package org.afob.limit;

import org.afob.execution.ExecutionClient;
import org.afob.limit.model.Order;
import org.afob.limit.service.Persistence;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class LimitOrderAgentTest {

    private ExecutionClient executionClient;
    private Persistence cachePersistenceService;
    private LimitOrderAgent limitOrderAgent;

    @Before
    public void setUp() {
        executionClient = mock(ExecutionClient.class);
        cachePersistenceService = mock(Persistence.class);
        limitOrderAgent = new LimitOrderAgent(executionClient, cachePersistenceService);
    }

    @Test
    public void testAddOrder() {
        String productId = "IBM";
        BigDecimal amount = new BigDecimal("100");
        BigDecimal price = new BigDecimal("1000.00");

        BigDecimal secondOrderAmount = new BigDecimal("100");
        BigDecimal secondOrderPrice = new BigDecimal("1500.00");

        limitOrderAgent.addOrder(true, productId, amount, price, true);
        limitOrderAgent.addOrder(false, productId, amount, price, true);
        limitOrderAgent.addOrder(true, productId, secondOrderAmount, secondOrderPrice, true);

        ConcurrentHashMap<String, LimitOrderAgent.PendingOrdersBook> orderMap = limitOrderAgent.getProductToOrdersMap();
        assertTrue(orderMap.containsKey(productId));

        var buyOrdersQueue = orderMap.get(productId).pendingBuyOrders;
        assertEquals(2, buyOrdersQueue.size());

        var sellOrdersQueue = orderMap.get(productId).pendingSellOrders;
        assertEquals(1, sellOrdersQueue.size());
    }

    @Test
    public void testPriceTickExecutesOrders() throws ExecutionClient.ExecutionException {
        String productId = "IBM";
        BigDecimal amount = new BigDecimal("100");
        BigDecimal price = new BigDecimal("1000.00");

        limitOrderAgent.addOrder(true, productId, amount, price, true);
        limitOrderAgent.priceTick(productId, price);

        verify(executionClient, timeout(1000)).buy(eq(productId), eq(amount.intValue()));
    }

    @Test
    public void testCancelOrder() {
        String productId = "IBM";
        BigDecimal amount = new BigDecimal("100");
        BigDecimal price = new BigDecimal("1000.00");

        limitOrderAgent.addOrder(true, productId, amount, price, true);
        limitOrderAgent.addOrder(false, productId, amount, price, true);

        var orderMap = limitOrderAgent.getProductToOrdersMap();
        var ordersBuyQueue = orderMap.get(productId).pendingBuyOrders.firstEntry().getValue();
        var ordersSellQueue = orderMap.get(productId).pendingSellOrders.firstEntry().getValue();

        Order buyOrder = ordersBuyQueue.peek();
        Order sellOrder = ordersSellQueue.peek();
        assertNotNull(buyOrder);
        assertNotNull(sellOrder);

        boolean buyCancelResult = limitOrderAgent.cancelOrder(buyOrder.getOrderId(), productId);
        assertTrue(buyCancelResult);
        assertTrue(ordersBuyQueue.isEmpty());

        boolean sellCancelResult = limitOrderAgent.cancelOrder(sellOrder.getOrderId(), productId);
        assertTrue(sellCancelResult);
        assertTrue(ordersSellQueue.isEmpty());
    }

    @Test
    public void testUpdateOrder() {
        String productId = "IBM";
        BigDecimal amount = new BigDecimal("100");
        BigDecimal price = new BigDecimal("1000.00");

        limitOrderAgent.addOrder(true, productId, amount, price, true);

        var orderMap = limitOrderAgent.getProductToOrdersMap();
        var ordersQueue = orderMap.get(productId).pendingBuyOrders.firstEntry().getValue();

        Order order = ordersQueue.peek();
        assertNotNull(order);

        BigDecimal newAmount = new BigDecimal("200");
        BigDecimal newPrice = new BigDecimal("155.00");

        boolean result = limitOrderAgent.updateOrder(order.getOrderId(), productId, newAmount, newPrice);
        assertTrue(result);
        assertEquals(newAmount.intValue(), order.getAmount());
        assertEquals(newPrice, order.getLimitPrice());
    }

    @Test
    public void testConcurrentOrderProcessing() throws InterruptedException {
        String productId = "IBM";
        BigDecimal price = new BigDecimal("1000.00");
        AtomicInteger successfulOrders = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100; i++) {
            executorService.execute(() -> {
                BigDecimal amount = new BigDecimal("10");
                limitOrderAgent.addOrder(true, productId, amount, price, true);
                successfulOrders.incrementAndGet();
            });
        }

        executorService.shutdown();
        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(100, successfulOrders.get());

        var orderMap = limitOrderAgent.getProductToOrdersMap();
        assertTrue(orderMap.containsKey(productId));
        assertFalse(orderMap.get(productId).pendingBuyOrders.isEmpty());
    }

    /**
     * 3 IBM buy order having limit price - 1000, 1001, 1000.5
     * 3 IBM sell order having limit price - 99, 99.5, 1000, 1005
     * For Market Price 1000 - 3 buy orders should be executed and 3 sell orders should be executed
     *     - Buy order - 1000, 1001, 1000.5 are triggered for execution
     *     - Sell order - 999, 999.5, 1000 are triggered for execution
     * @throws InterruptedException
     * @throws ExecutionClient.ExecutionException
     */
    @Test
    public void testPriceTickInvokesBuyMultipleTimes() throws InterruptedException, ExecutionClient.ExecutionException {
        String productId = "IBM";
        BigDecimal marketPrice = new BigDecimal("1000.00");

        //Some buy orders
        limitOrderAgent.addOrder(true, productId, BigDecimal.valueOf(100), BigDecimal.valueOf(1000), true);
        limitOrderAgent.addOrder(true, productId, BigDecimal.valueOf(100), BigDecimal.valueOf(1001), true);
        limitOrderAgent.addOrder(true, productId, BigDecimal.valueOf(100), BigDecimal.valueOf(1000.5), true);

        //Some sell orders
        limitOrderAgent.addOrder(false, productId, BigDecimal.valueOf(100), BigDecimal.valueOf(999), true);
        limitOrderAgent.addOrder(false, productId, BigDecimal.valueOf(100), BigDecimal.valueOf(999.5), true);
        limitOrderAgent.addOrder(false, productId, BigDecimal.valueOf(100), BigDecimal.valueOf(1000), true);
        limitOrderAgent.addOrder(false, productId, BigDecimal.valueOf(100), BigDecimal.valueOf(1005), true);

        limitOrderAgent.priceTick(productId, marketPrice);

        verify(executionClient, timeout(1000).times(3)).buy(eq(productId), anyInt());
        verify(executionClient, timeout(1000).times(3)).sell(eq(productId), anyInt());
    }

    @Test
    public void testPriceTickInvokesBuyMultipleTimesForMultipleStocks() throws InterruptedException, ExecutionClient.ExecutionException {
        String ibmProduct = "IBM";
        BigDecimal ibmMarketPrice = new BigDecimal("1000.00");

        String teslaProduct = "TSLA";
        BigDecimal teslaMarketPrice = new BigDecimal("500.00");

        //Some buy  and sell IBM orders
        limitOrderAgent.addOrder(true, ibmProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(1000), true);
        limitOrderAgent.addOrder(true, ibmProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(1001), true);
        limitOrderAgent.addOrder(true, ibmProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(1000.5), true);
        limitOrderAgent.addOrder(false, ibmProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(99), true);
        limitOrderAgent.addOrder(false, ibmProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(99.5), true);
        limitOrderAgent.addOrder(false, ibmProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(1000), true);
        limitOrderAgent.addOrder(false, ibmProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(1005), true);

        //Some buy and sell TSLA orders
        limitOrderAgent.addOrder(true, teslaProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(500), true);
        limitOrderAgent.addOrder(true, teslaProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(501), true);
        limitOrderAgent.addOrder(false, teslaProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(499), true);
        limitOrderAgent.addOrder(false, teslaProduct, BigDecimal.valueOf(100), BigDecimal.valueOf(499.5), true);


        limitOrderAgent.priceTick(ibmProduct, ibmMarketPrice);
        verify(executionClient, timeout(1000).times(3)).buy(eq(ibmProduct), anyInt());
        verify(executionClient, timeout(1000).times(3)).sell(eq(ibmProduct), anyInt());

        limitOrderAgent.priceTick(teslaProduct, teslaMarketPrice);
        verify(executionClient, timeout(1000).times(2)).buy(eq(teslaProduct), anyInt());
        verify(executionClient, timeout(1000).times(2)).sell(eq(teslaProduct), anyInt());
    }
}
