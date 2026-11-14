// Issue 0274. Make this test pass by fixing the application code, not the test.
package com.shopcore.reports;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.catalog.Catalog;
import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class Issue0274Test {

  private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
  private static final Money ZERO = Money.zero(Currency.INR);

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static Sale sale(String order, String sku, int qty, String net, String at) {
    return new Sale(order, sku, qty, inr(net), Instant.parse(at));
  }

  private static LocalDate nov(int d) {
    return LocalDate.of(2026, 11, d);
  }

  private static final List<Sale> SALES =
      List.of(
          sale("O1", "APP-102346-BLK-M", 2, "998", "2026-11-01T05:00:00Z"),
          sale("O1", "BOK-000000", 1, "650", "2026-11-01T05:00:00Z"),
          sale("O2", "ELE-555012", 1, "2499", "2026-11-01T20:00:00Z"),
          sale("O3", "APP-102346-BLK-M", 1, "499", "2026-11-02T10:00:00Z"),
          sale("O4", "TOY-300001", 1, "100", "2026-11-03T12:00:00Z"));

  @Test
  void test_issue_0274() {
    assertAll(
        "issue 0274",
        () -> {
          assertEquals(Optional.of(nov(2)), SalesReport.bestDay(SALES, IST));
        },
        () -> {
          List<Sale> s =
              List.of(
                  sale("A", "X", 1, "5", "2026-11-02T05:00:00Z"),
                  sale("B", "X", 1, "5", "2026-11-01T05:00:00Z"));
          assertEquals(Optional.of(nov(1)), SalesReport.bestDay(s, IST));
        },
        () -> {
          assertEquals(Optional.empty(), SalesReport.bestDay(List.of(), IST));
        });
  }
}
