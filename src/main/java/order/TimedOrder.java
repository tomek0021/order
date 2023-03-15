package order;

import java.time.LocalDateTime;

class TimedOrder implements Comparable<TimedOrder> {
    private long id;
    private double price;
    private Side side;
    private long size;

    private LocalDateTime time;

    TimedOrder(long id, double price, Side side, long size) {
        this.id = id;
        this.price = price;
        this.size = size;
        this.side = side;
        this.time = LocalDateTime.now(); // timezone ommitted for simplification
    }

    static TimedOrder from(Order order) {
        Side side = Side.from(order.getSide());

        return new TimedOrder(
                order.getId(),
                order.getPrice(),
                side,
                order.getSize()
        );
    }

    TimedOrder modifySize(long newSize) {
        return new TimedOrder(
                this.id,
                this.price,
                this.side,
                newSize
        );
    }

    Order toOrder() {
        return new Order(
                this.id,
                this.price,
                this.side.toChar(),
                this.size
        );
    }

    boolean isBid() {
        return side == Side.Bid;
    }

    boolean isOffer() {
        return side == Side.Offer;
    }

    public long getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public long getSize() {
        return size;
    }

    @Override
    public int compareTo(TimedOrder o) {
        int priceCompare = Double.compare(this.price, o.price);
        if (isBid()) {
            priceCompare *= -1;
        }
        if (priceCompare == 0) {
            return this.time.compareTo(o.time);
        }
        return priceCompare;
    }
}
