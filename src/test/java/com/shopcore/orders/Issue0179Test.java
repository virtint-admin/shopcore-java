// Issue 0179. Make this test pass by fixing the application code, not the test.
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

class Issue0179Test {

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
  void test_issue_0179() {
    assertAll(
        "issue 0179",
        () -> {
          Order o = delivered();
          o.returnItems(Map.of(TEE, 1), at(72));
          assertEquals(inr("1149"), o.returnItems(Map.of(TEE, 1, BOOK, 1), at(73)));
          assertEquals(OrderStatus.RETURNED, o.status());
          assertEquals(inr("50"), o.refundableAmount());
        },
        () -> {
          Order o = delivered();
          o.returnItems(Map.of(TEE, 1), at(72));
          assertEquals(1, o.returnableQuantity(TEE));
        },
        () -> {
          Order o = delivered();
          assertThrows(IllegalArgumentException.class, () -> o.returnItems(Map.of(TEE, 3), at(72)));
          assertThrows(IllegalArgumentException.class, () -> o.returnItems(Map.of(TEE, 0), at(72)));
          assertThrows(
              IllegalArgumentException.class, () -> o.returnItems(Map.of("ELE-555012", 1), at(72)));
          assertEquals(inr("0"), o.refundedAmount());
          assertThrows(IllegalStateException.class, () -> paid().returnItems(Map.of(TEE, 1), at(72)));
        },
        () -> {
          Order o = delivered();
          Instant deadline = o.returnDeadline().orElseThrow();
          assertEquals(inr("650"), o.returnItems(Map.of(BOOK, 1), deadline));
          Order late = delivered();
          assertThrows(
              IllegalStateException.class,
              () -> late.returnItems(Map.of(BOOK, 1), deadline.plusSeconds(1)));
        });
  }
}
