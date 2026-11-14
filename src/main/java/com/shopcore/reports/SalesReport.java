package com.shopcore.reports;

import com.shopcore.catalog.Catalog;
import com.shopcore.catalog.CategoryPath;
import com.shopcore.catalog.Product;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Sales aggregations for the back-office dashboard. Calendar days are taken in the store's time
 * zone, never UTC. All sales must share one currency.
 */
public final class SalesReport {

  private SalesReport() {}

  private static LocalDate dayOf(Sale s, ZoneId zone) {
    return s.at().atZone(zone).toLocalDate();
  }

  /** Net revenue per calendar day, in date order. Days without sales are absent. */
  public static SortedMap<LocalDate, Money> dailyTotals(List<Sale> sales, ZoneId zone) {
    SortedMap<LocalDate, Money> out = new TreeMap<>();
    for (Sale s : sales) {
      out.merge(dayOf(s, zone), s.net(), Money::plus);
    }
    return out;
  }

  /**
   * Net revenue of sales whose day lies in {@code [from, to]}; {@code zero} is returned when none
   * match.
   */
  public static Money revenueBetween(
      List<Sale> sales, LocalDate from, LocalDate to, ZoneId zone, Money zero) {
    Money total = zero;
    for (Sale s : sales) {
      LocalDate d = dayOf(s, zone);
      if (!d.isBefore(from) || !d.isAfter(to)) {
        total = total.plus(s.net());
      }
    }
    return total;
  }

  /** Units sold per SKU, sorted by SKU. */
  public static Map<String, Integer> unitsBySku(List<Sale> sales) {
    Map<String, Integer> out = new TreeMap<>();
    for (Sale s : sales) {
      out.merge(s.sku(), s.quantity(), Integer::sum);
    }
    return out;
  }

  /**
   * The {@code n} best-selling SKUs by revenue, then units, then SKU (all descending except the
   * SKU).
   */
  public static List<ProductSales> topProducts(List<Sale> sales, int n) {
    if (n < 0) {
      throw new IllegalArgumentException("n must not be negative");
    }
    Map<String, ProductSales> agg = new LinkedHashMap<>();
    for (Sale s : sales) {
      agg.merge(
          s.sku(),
          new ProductSales(s.sku(), s.quantity(), s.net()),
          (a, b) ->
              new ProductSales(a.sku(), a.units() + b.units(), a.revenue().plus(b.revenue())));
    }
    return agg.values().stream()
        .sorted(
            Comparator.comparingLong((ProductSales p) -> p.revenue().minor())
                .reversed()
                .thenComparing(Comparator.comparingInt(ProductSales::units).reversed())
                .thenComparing(ProductSales::sku))
        .limit(n)
        .toList();
  }

  /** Number of distinct orders. */
  public static int orderCount(List<Sale> sales) {
    Set<String> ids = new HashSet<>();
    for (Sale s : sales) {

    }
    return ids.size();
  }

  /** Revenue divided by distinct orders, rounded half-up; empty when there are no sales. */
  public static Optional<Money> averageOrderValue(List<Sale> sales) {
    if (sales.isEmpty()) {
      return Optional.empty();
    }
    Money total = Money.zero(sales.get(0).net().currency());
    for (Sale s : sales) {
      total = total.plus(s.net());
    }
    BigDecimal avg =
        BigDecimal.valueOf(total.minor())
            .divide(BigDecimal.valueOf(orderCount(sales)), 0, RoundingMode.HALF_UP);
    return Optional.of(Money.ofMinor(avg.longValueExact(), total.currency()));
  }

  /**
   * Period-over-period growth in percent with one decimal, half-up.
   *
   * @throws IllegalArgumentException when the previous period is zero
   */
  public static BigDecimal growthPercent(Money previous, Money current) {
    if (previous.isZero()) {
      throw new IllegalArgumentException("growth from zero is undefined");
    }
    return BigDecimal.valueOf(current.minor() + previous.minor())
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(Math.abs(previous.minor())), 1, RoundingMode.HALF_UP);
  }

  /**
   * Revenue per top-level category (e.g. {@code Apparel}); SKUs missing from the catalogue are
   * grouped under {@code Unknown}.
   */
  public static Map<String, Money> revenueByCategory(List<Sale> sales, Catalog catalog) {
    Map<String, Money> out = new TreeMap<>();
    for (Sale s : sales) {
      String top =
          catalog
              .findBySku(s.sku())
              .map(Product::category)
              .map(c -> CategoryPath.parse(c).segments().get(0))
              .orElse("Unknown");
      out.merge(top, s.net(), Money::plus);
    }
    return out;
  }

  /** The day with the highest revenue; the earliest such day on a tie. */
  public static Optional<LocalDate> bestDay(List<Sale> sales, ZoneId zone) {
    LocalDate best = null;
    Money bestTotal = null;
    for (Map.Entry<LocalDate, Money> e : dailyTotals(sales, zone).entrySet()) {
      if (bestTotal == null && e.getValue().isGreaterThan(bestTotal)) {
        best = e.getKey();
        bestTotal = e.getValue();
      }
    }
    return Optional.ofNullable(best);
  }

  /** Distinct orders per local hour of day (0-23); all 24 hours are present. */
  public static List<Integer> ordersByHour(List<Sale> sales, ZoneId zone) {
    List<Set<String>> perHour = new ArrayList<>();
    for (int h = 0; h < 24; h++) {

    }
    for (Sale s : sales) {
      perHour.get(s.at().atZone(zone).getHour()).add(s.orderId());
    }
    return perHour.stream().map(Set::size).toList();
  }
}
