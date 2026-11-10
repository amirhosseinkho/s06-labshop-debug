package labshop;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** Pricing rules for LabShop orders. All amounts are in toman. */
public final class Pricing {
    static final long BULK_MIN_QTY = 10;
    static final long BULK_PERCENT = 10;
    static final long TAX_PERCENT = 9;
    static final long SHIPPING_FEE = 40_000;
    static final long FREE_SHIPPING_FROM = 1_000_000;

    private static final Map<String, Coupon> COUPONS = Map.of(
            "OFF10", new Coupon("percent", 10),
            "OFF20", new Coupon("percent", 20),
            "FIX50", new Coupon("fixed", 50_000));

    private Pricing() {
    }

    public static Coupon findCoupon(String code) {
        return COUPONS.get(code.trim().toUpperCase(java.util.Locale.ROOT));
    }

    public static Bill priceOrder(Order order) {
        long subtotal = 0;
        for (Item item : order.getItems()) {
            subtotal += item.getUnitPrice() * item.getQty();
        }
        long bulk = bulkDiscount(order.getItems());
        long coupon = couponDiscount(order, subtotal - bulk);
        long taxable = subtotal - bulk - coupon;
        long tax = taxFor(taxable);
        long shipping = shippingFor(taxable);
        return new Bill(subtotal, bulk + coupon, tax, shipping, taxable + tax + shipping);
    }

    // 10% off every line that has BULK_MIN_QTY units or more
    static long bulkDiscount(List<Item> items) {
        long discount = 0;
        Map<String, long[]> totalsBySku = new HashMap<>();
        for (Item item : items) {
            long[] totals = totalsBySku.computeIfAbsent(item.getSku(), sku -> new long[2]);
            totals[0] += item.getQty();
            totals[1] += item.getUnitPrice() * item.getQty();
        }
        for (long[] totals : totalsBySku.values()) {
            if (totals[0] >= BULK_MIN_QTY) {
                discount += totals[1] * BULK_PERCENT / 100;
            }
        }
        return discount;
    }

    static long couponDiscount(Order order, long amount) {
        String code = order.getCouponCode().trim();
        if (code.isEmpty()) {
            return 0;
        }
        Coupon coupon = findCoupon(code);
        if (coupon == null) {
            return 0;
        }
        if (coupon.kind().equals("fixed")) {
            return Math.min(coupon.value(), amount);
        }
        return amount * coupon.value() / 100;
    }

    // VAT, rounded to the nearest toman
    public static long taxFor(long amount) {
        return (amount * TAX_PERCENT + 50) / 100;
    }

    public static long shippingFor(long amount) {
        if (amount >= FREE_SHIPPING_FROM) {
            return 0;
        }
        return SHIPPING_FEE;
    }
}
