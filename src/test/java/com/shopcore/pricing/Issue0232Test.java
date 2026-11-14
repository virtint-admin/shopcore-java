// Issue 0232. Make this test pass by fixing the application code, not the test.
package com.shopcore.pricing;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0232Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static BigDecimal pct(String p) {
    return new BigDecimal(p);
  }

  @Test
  void test_issue_0232() {
    assertAll(
        "issue 0232",
        () -> {
          assertEquals(23, Discounts.savingsPercent(inr("1000"), inr("770")));
        },
        () -> {
          assertEquals(49, Discounts.savingsPercent(inr("1999"), inr("1000")));
        },
        () -> {
          assertEquals(100, Discounts.savingsPercent(inr("100"), inr("0")));
        },
        () -> {
          assertEquals(0, Discounts.savingsPercent(inr("100"), inr("100")));
          assertEquals(0, Discounts.savingsPercent(inr("100"), inr("120")));
        });
  }
}
