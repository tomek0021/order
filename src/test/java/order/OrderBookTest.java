package order;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static order.OrderFixtures.newBid;
import static order.OrderFixtures.withSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class OrderBookTest {

    OrderBookTest() {
        Awaitility.setDefaultTimeout(Duration.ofSeconds(5));
    }

    Order bid100 = newBid(100.5, 11);
    Order bid200 = newBid(200.5, 12);
    Order bid10 = newBid(10.5, 50);

    @Test
    void getAllOfReturnsOrdersSortedByPriceAndTime() throws InterruptedException {
        // given
        var orderBook = new OrderBook();
        Thread.sleep(1L);
        Order order100_2 = newBid(bid100.getPrice(), bid100.getSize());
        orderBook.add(bid100);
        orderBook.add(bid200);
        orderBook.add(bid10);
        orderBook.add(order100_2);

        // when then
        await().untilAsserted(() ->
                assertThat(orderBook.getAllOf(Side.Bid.toChar()))
                        .containsExactly(bid200, bid100, order100_2, bid10)
        );
    }

    @Test
    void removingOrderRemovesIt() {
        // given
        var orderBook = new OrderBook();
        orderBook.add(bid100);
        orderBook.add(bid200);
        orderBook.add(bid10);
        waitForBookToContain(orderBook, bid200, bid100, bid10);

        // when
        orderBook.remove(bid200.getId());

        // then
        await().untilAsserted(() ->
                assertThat(orderBook.getAllOf(Side.Bid.toChar()))
                        .containsExactly(bid100, bid10)
        );
    }

    @Test
    void modifyingOrderModifiesIt() {
        // given
        var orderBook = new OrderBook();
        orderBook.add(bid100);
        orderBook.add(bid200);
        orderBook.add(bid10);
        waitForBookToContain(orderBook, bid200, bid100, bid10);
        var modified = withSize(bid200, bid200.getSize() + 2);

        // when
        orderBook.modify(bid200.getId(), modified.getSize());

        // then
        await().untilAsserted(() ->
                assertThat(orderBook.getAllOf(Side.Bid.toChar()))
                        .containsExactly(modified, bid100, bid10)
        );
    }

    @Test
    void getPriceReturnsPriceForGivenLevel() {
        // given
        var orderBook = new OrderBook();
        orderBook.add(bid100);
        orderBook.add(bid200);
        orderBook.add(bid10);
        waitForBookToContain(orderBook, bid200, bid100, bid10);

        // when
        var price = orderBook.getPrice(Side.Bid.toChar(), 1);

        // then
        assertThat(price).isEqualTo(bid200.getPrice());

        // when 2
        var price2 = orderBook.getPrice(Side.Bid.toChar(), 2);

        // then 2
        assertThat(price2).isEqualTo(bid100.getPrice());
    }

    @Test
    void getTotalSizeAvailableReturnsTotalPriceForGivenLevel() {
        // given
        var orderBook = new OrderBook();
        orderBook.add(bid100);
        orderBook.add(bid200);
        orderBook.add(bid10);
        waitForBookToContain(orderBook, bid200, bid100, bid10);

        // when
        var totalSizeAvailable = orderBook.getTotalSizeAvailable(Side.Bid.toChar(), 2);

        // then
        assertThat(totalSizeAvailable).isEqualTo(bid200.getSize() + bid100.getSize());

        // when 2
        var totalSizeAvailable2 = orderBook.getTotalSizeAvailable(Side.Bid.toChar(), 3);

        // then 2
        assertThat(totalSizeAvailable2).isEqualTo(bid200.getSize() + bid100.getSize() + bid10.getSize());
    }

    private void waitForBookToContain(OrderBook orderBook, Order... orders) {
        await().untilAsserted(() ->
                assertThat(orderBook.getAllOf(Side.Bid.toChar()))
                        .containsExactly(orders)
        );
    }
}