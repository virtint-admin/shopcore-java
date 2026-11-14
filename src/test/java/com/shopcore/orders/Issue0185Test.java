// Issue 0185. Make this test pass by fixing the application code, not the test.
package com.shopcore.orders;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class Issue0185Test {

  private static final String TEE = "APP-102346-BLK-M";
  private static final String BOOK = "BOK-000000";
  private static final Instant T0 = Instant.parse("2026-11-01T09:00:00Z");

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static Instant at(int hours) {
    return T0.plus(Duration.ofHours(hours));
  }

  private static Order order() {
    return new Order(
        "SC-20261101-00001",
        List.of(new OrderLine(TEE, inr("499"), 2), new OrderLine(BOOK, inr("650"), 1)),
        inr("50"),
        T0);
  }

  private static Order paid() {
    Order o = order();
    o.pay(inr("1698"), at(1));
    return o;
  }

  private static Order delivered() {
    Order o = paid();
    o.pack(at(2));
    o.ship("DLV123", at(3));
    o.deliver(at(48));
    return o;
  }

  @Test
  void test_issue_0185() {
    assertAll(
        "issue 0185",
        () -> {
          Order o = paid();
          o.refund(inr("1697.99"));
          assertFalse(o.isFullyRefunded());
        },
        () -> {
          Order o = order();
          o.cancel("x", at(1));
          assertFalse(o.isFullyRefunded());
        },
        () -> {
          Order o = paid();
          o.refund(inr("1698"));
          assertTrue(o.isFullyRefunded());
        });
  }
}
