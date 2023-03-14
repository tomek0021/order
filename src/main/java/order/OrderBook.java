package order;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OrderBook {

    private static final Logger logger = LoggerFactory.getLogger(OrderBook.class);


    private final ConcurrentHashMap<Long, TimedOrder> orders = new ConcurrentHashMap<>();

    private final AsynchronousOrdersSorter ordersSorter = new AsynchronousOrdersSorter(orders);

    public void add(Order order) {
        TimedOrder timedOrder = TimedOrder.from(order);

        orders.put(timedOrder.getId(), timedOrder);
        ordersSorter.onSourceCollectionChanged(timedOrder);
    }

    public void remove(long orderId) {
        var timedOrder = orders.remove(orderId);
        ordersSorter.onSourceCollectionChanged(timedOrder);
    }

    public void modify(long orderId, long newSize) {
        AtomicReference<TimedOrder> ref = new AtomicReference();
        orders.computeIfPresent(orderId, (id, order) -> {
            ref.set(order.modifySize(newSize));
            return ref.get();
        });
        ordersSorter.onSourceCollectionChanged(ref.get());
    }

    public double getPrice(char sideChar, int level) {
        levelValidation(level);

        List<TimedOrder> sortedOrders = ordersSorter.getSortedOrdersForSide(sideChar);

        int index = level - 1;
        return sortedOrders.get(index).getPrice();
    }

    private void levelValidation(int level) {
        if (level <= 0) throw new IllegalArgumentException("Level must be >0, but is " + level);
    }

    public double getTotalSizeAvailable(char sideChar, int level) {
        levelValidation(level);

        List<TimedOrder> sortedOrders = ordersSorter.getSortedOrdersForSide(sideChar);

        long totalSize = 0;
        for (int i = 0; i < level; i++) {
            totalSize += sortedOrders.get(i).getSize();
        }
        return totalSize;
    }

    public List<Order> getAllOf(char sideChar) {
        List<TimedOrder> sortedOrders = ordersSorter.getSortedOrdersForSide(sideChar);
        return sortedOrders.stream()
                .map(TimedOrder::toOrder)
                .collect(Collectors.toList());
    }

    private static class AsynchronousOrdersSorter {
        private final ExecutorService executorService = Executors.newFixedThreadPool(2);

        private final SortingOrdersThread sortedBids;
        private final SortingOrdersThread sortedOffers;

        public AsynchronousOrdersSorter(ConcurrentHashMap<Long, TimedOrder> ordersSource) {
            this.sortedBids = new SortingOrdersThread(ordersSource, TimedOrder::isBid);
            this.sortedOffers = new SortingOrdersThread(ordersSource, TimedOrder::isOffer);

            executorService.submit(sortedBids);
            executorService.submit(sortedOffers);
        }

        void onSourceCollectionChanged(TimedOrder timedOrder) {
            var sortingOrdersThread = timedOrder.isBid() ? sortedBids : sortedOffers;
            sortingOrdersThread.onSourceCollectionChanged();
        }

        List<TimedOrder> getSortedOrdersForSide(char sideChar) {
            Side side = Side.from(sideChar);
            return side == Side.Bid ? sortedBids.getSortedOrders() : sortedOffers.getSortedOrders();
        }
    }

    private static class SortingOrdersThread implements Runnable {

        private final ConcurrentHashMap<Long, TimedOrder> ordersSource;
        private final Predicate<TimedOrder> filterPredicate;
        private final AtomicReference<List<TimedOrder>> sortedOrders = new AtomicReference(new ArrayList<>());
        private final BlockingQueue<Long> sortRequest = new LinkedBlockingQueue<>(1);

        private SortingOrdersThread(ConcurrentHashMap<Long, TimedOrder> ordersSource,
                                    Predicate<TimedOrder> filterPredicate) {
            this.ordersSource = ordersSource;
            this.filterPredicate = filterPredicate;
        }

        void onSourceCollectionChanged() {
            sortRequest.offer(1L);
        }

        List<TimedOrder> getSortedOrders() {
            return sortedOrders.get();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sortRequest.take();

                    Collection<TimedOrder> timedOrders = this.ordersSource.values();
                    List<TimedOrder> ordersSnapshot = new ArrayList<>(timedOrders.stream()
                            .filter(filterPredicate)
                            .toList());
                    Collections.sort(ordersSnapshot);
                    sortedOrders.set(ordersSnapshot);

                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.warn("Sorting thread interrupted, exiting.");
                    return;
                } catch (Exception e) {
                    logger.error("Exception on sorting", e);
                }
            }
        }
    }
}
