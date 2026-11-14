// Issue 0310. Make this test pass by fixing the application code, not the test.
package com.shopcore.tax;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0310Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static BigDecimal pct(String p) {
    return new BigDecimal(p);
  }

  @Test
  void test_issue_0310() {
    assertAll(
        "issue 0310",
        () -> {
          assertEquals(
              new GstSplit(inr("0"), inr("0"), inr("18.01")),
              TaxCalculator.splitGst(inr("18.01"), false));
        },
        () -> {
          assertEquals(
              new GstSplit(inr("9"), inr("9"), inr("0")), TaxCalculator.splitGst(inr("18"), true));
        },
        () -> {
          assertEquals(
              new GstSplit(inr("9.00"), inr("9.01"), inr("0")),
              TaxCalculator.splitGst(inr("18.01"), true));
        });
  }
}
