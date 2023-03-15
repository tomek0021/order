package order;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class TimedOrderTest {

    @ParameterizedTest
    @CsvSource({
            "Bid,100.1,1000000,Bid,100.1,1000000,0",
            "Bid,100.1,1000000,Bid,100.2,1000000,1",
            "Bid,100.1,1000000,Bid,100.1,1000001,-1",
            "Bid,100.2,1000000,Bid,100.1,1000000,-1",
            "Bid,100.1,1000001,Bid,100.1,1000000,1",
            "Offer,100.1,1000000,Bid,100.1,1000000,0",
            "Offer,100.1,1000000,Bid,100.2,1000000,-1",
            "Offer,100.1,1000000,Bid,100.1,1000001,-1",
            "Offer,100.2,1000000,Bid,100.1,1000000,1",
            "Offer,100.1,1000001,Bid,100.1,1000000,1",
    })
    void compareToUsesSideAndPriceAndTime(Side side1, double price1, long time1,
                                          Side side2, double price2, long time2,
                                          int expected) {
        int size = 10;
        var order1 = new TimedOrder(1, price1, side1, size);
        setTimeField(order1, time1);
        var order2 = new TimedOrder(2, price2, side2, size);
        setTimeField(order2, time2);

        assertThat(order1.compareTo(order2)).isEqualTo(expected);
    }

    private void setTimeField(TimedOrder order, long time1) {
        LocalDateTime date =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(time1), ZoneId.systemDefault());

        Field timeField = Arrays.stream(TimedOrder.class.getDeclaredFields())
                .filter(field -> field.getName().equals("time"))
                .findFirst()
                .get();
        timeField.setAccessible(true);
        try {
            timeField.set(order, date);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}