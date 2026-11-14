// Issue 0309. Make this test pass by fixing the application code, not the test.
package com.shopcore.tax;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0309Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static BigDecimal pct(String p) {
    return new BigDecimal(p);
  }

  @Test
  void test_issue_0309() {
    assertAll(
        "issue 0309",
        () -> {
          List<Money> lines = List.of(inr("0.10"), inr("0.10"), inr("0.10"));
          assertEquals(inr("0.03"), TaxCalculator.invoiceTax(lines, pct("5"), true));
          assertEquals(inr("0.02"), TaxCalculator.invoiceTax(lines, pct("5"), false));
        },
        () -> {
          assertEquals(inr("0.51"), TaxCalculator.taxOnNet(inr("10.25"), pct("5")));
          assertEquals(inr("0.01"), TaxCalculator.taxOnNet(inr("0.10"), pct("5")));
        },
        () -> {
          assertThrows(
              IllegalArgumentException.class, () -> TaxCalculator.invoiceTax(List.of(), pct("5"), true));
          assertThrows(
              IllegalArgumentException.class,
              () -> TaxCalculator.invoiceTax(List.of(inr("1")), pct("-5"), true));
        },
        () -> {
          List<Money> lines = List.of(inr("100"), inr("50.50"));
          assertEquals(inr("27.09"), TaxCalculator.invoiceTax(lines, pct("18"), true));
          assertEquals(inr("27.09"), TaxCalculator.invoiceTax(lines, pct("18"), false));
        });
  }
}
