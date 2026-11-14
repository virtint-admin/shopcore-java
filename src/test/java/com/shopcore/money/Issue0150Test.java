// Issue 0150. Make this test pass by fixing the application code, not the test.
package com.shopcore.money;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Issue0150Test {

  private static final FxRates RATES = FxRates.standard();

  @Test
  void test_issue_0150() {
    assertAll(
        "issue 0150",
        () -> {
          FxRates rates = FxRates.fromTable(Currency.USD, Map.of(Currency.INR, new BigDecimal("80")));
          assertEquals(BigDecimal.ONE, rates.rate(Currency.USD, Currency.USD));
          assertEquals(new BigDecimal("80"), rates.rate(Currency.USD, Currency.INR));
        },
        () -> {
          assertThrows(
              IllegalArgumentException.class,
              () -> FxRates.fromTable(Currency.USD, Map.of(Currency.INR, BigDecimal.ZERO)));
        },
        () -> {
          FxRates rates = FxRates.fromTable(Currency.USD, Map.of(Currency.INR, new BigDecimal("80")));
          assertThrows(IllegalArgumentException.class, () -> rates.rate(Currency.USD, Currency.EUR));
        },
        () -> {
          for (Currency c : Currency.values()) {
            assertEquals(1, RATES.rate(c, c).intValueExact());
            RATES.rate(Currency.USD, c);
          }
        },
        () -> {
          assertEquals(new BigDecimal("0.3075"), RATES.rate(Currency.USD, Currency.KWD));
        },
        () -> {
          assertEquals(new BigDecimal("83.25"), RATES.rate(Currency.USD, Currency.INR));
        });
  }
}
