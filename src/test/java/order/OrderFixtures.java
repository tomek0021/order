package order;

import java.util.concurrent.atomic.AtomicLong;

public class OrderFixtures {

    private static final AtomicLong ids = new AtomicLong(1);

    public static Order newBid(double price, long size) {
        return newOrder(price, Side.Bid, size);
    }

    public static Order newOffer(double price, long size) {
        return newOrder(price, Side.Offer, size);
    }

    private static Order newOrder(double price, Side side, long size) {
        return new Order(ids.getAndIncrement(), price, side.toChar(), size);
    }

    public static Order withSize(Order order, long size) {
        return new Order(order.getId(), order.getPrice(), order.getSide(), size);
    }
}
