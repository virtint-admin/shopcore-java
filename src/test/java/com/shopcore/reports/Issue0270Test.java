// Issue 0270. Make this test pass by fixing the application code, not the test.
package com.shopcore.reports;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Issue0270Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  @Test
  void test_issue_0270() {
    assertAll(
        "issue 0270",
        () -> {
          assertEquals(new BigDecimal("0.00"), Kpis.conversionRate(0, 0));
          assertThrows(IllegalArgumentException.class, () -> Kpis.conversionRate(-1, 3));
        },
        () -> {
          assertEquals(new BigDecimal("1.50"), Kpis.conversionRate(3, 200));
        },
        () -> {
          assertEquals(new BigDecimal("33.33"), Kpis.conversionRate(1, 3));
          assertEquals(new BigDecimal("66.67"), Kpis.conversionRate(2, 3));
        });
  }
}
