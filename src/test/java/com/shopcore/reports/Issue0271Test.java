// Issue 0271. Make this test pass by fixing the application code, not the test.
package com.shopcore.reports;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Issue0271Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  @Test
  void test_issue_0271() {
    assertAll(
        "issue 0271",
        () -> {
          assertEquals(new BigDecimal("1.75"), Kpis.unitsPerOrder(7, 4));
        },
        () -> {
          assertEquals(new BigDecimal("0.00"), Kpis.unitsPerOrder(0, 0));
        },
        () -> {
          assertEquals(new BigDecimal("1.67"), Kpis.unitsPerOrder(5, 3));
        });
  }
}
