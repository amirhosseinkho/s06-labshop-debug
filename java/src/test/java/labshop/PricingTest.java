package labshop;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class PricingTest {
    private static Order order(String couponCode, Item... items) {
        Order order = new Order(1, "test", couponCode);
        order.getItems().addAll(List.of(items));
        return order;
    }

    @Test
    void orderWithoutCoupon() {
        Bill bill = Pricing.priceOrder(order("", new Item("A01", 100_000, 2), new Item("B01", 50_000, 1)));
        assertEquals(new Bill(250_000, 0, 22_500, 40_000, 312_500), bill);
    }

    @Test
    void fixedCoupon() {
        Bill bill = Pricing.priceOrder(order("FIX50", new Item("A01", 300_000, 1)));
        assertEquals(50_000, bill.discount());
        assertEquals(312_500, bill.total());
    }

    @Test
    void percentCoupon() {
        Bill bill = Pricing.priceOrder(order("OFF10", new Item("A01", 200_000, 1)));
        assertEquals(20_000, bill.discount());
        assertEquals(236_200, bill.total());
    }

    @Test
    void freeShippingFromOneMillion() {
        assertEquals(40_000, Pricing.shippingFor(999_990));
        assertEquals(0, Pricing.shippingFor(1_000_000));
    }

    @Test
    void bulkDiscountForOrder1047() {
        Order order = new Order(1047, "parisa", "");

        order.getItems().add(new Item("K07", 38_500, 6));
        order.getItems().add(new Item("M21", 12_990, 1));
        order.getItems().add(new Item("K07", 38_500, 6));

        Bill bill = Pricing.priceOrder(order);

        assertEquals(474_990, bill.subtotal());
        assertEquals(46_200, bill.discount());
        assertEquals(38_591, bill.tax());
        assertEquals(40_000, bill.shipping());
        assertEquals(507_381, bill.total());
    }
}
