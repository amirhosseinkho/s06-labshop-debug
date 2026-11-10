package labshop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Prints the LabShop daily report: java -cp target/classes labshop.Report ../data/orders.csv */
public class Report {
    static final String HEADER = "order  customer     subtotal  discount       tax  shipping       total";

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: labshop.Report <orders.csv>");
            System.exit(2);
        }
        List<Order> orders = readOrders(Path.of(args[0]));
        System.out.println("LabShop daily report");
        System.out.println(HEADER);
        int invalid = 0;
        for (Order order : orders) {
            Bill bill = Pricing.priceOrder(order);
            System.out.printf(Locale.ROOT, "%5d  %-10s %10d %9d %9d %9d %11d%n",
                    order.getId(), order.getCustomer(), bill.subtotal(), bill.discount(),
                    bill.tax(), bill.shipping(), bill.total());
            if (!order.getCouponCode().trim().isEmpty() && Pricing.findCoupon(order.getCouponCode()) == null) {
                invalid++;
            }
        }
        System.out.println("orders: " + orders.size());
        System.out.println("invalid coupons: " + invalid);
        System.out.println("grand total: " + grandTotal(orders));
    }

    static long grandTotal(List<Order> orders) {
        long sum = 0;
        for (Order order : orders) {
            sum += Pricing.priceOrder(order).total();
        }
        return sum;
    }

    static List<Order> readOrders(Path path) throws IOException {
        List<Order> orders = new ArrayList<>();
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        for (String line : lines.subList(1, lines.size())) {
            if (line.isEmpty()) {
                continue;
            }
            String[] fields = line.split(",", -1);
            Order order = new Order(Integer.parseInt(fields[0]), fields[1], fields[2]);
            for (String part : fields[3].split("\\|")) {
                String[] p = part.split(":");
                order.getItems().add(new Item(p[0], Long.parseLong(p[1]), Long.parseLong(p[2])));
            }
            orders.add(order);
        }
        return orders;
    }
}
