// Issue 0242. Make this test pass by fixing the application code, not the test.
package com.shopcore.pricing;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import org.junit.jupiter.api.Test;

class Issue0242Test {

  private static Money usd(String a) {
    return Money.of(a, Currency.USD);
  }

  @Test
  void test_issue_0242() {
    assertAll(
        "issue 0242",
        () -> {
          assertEquals(usd("12.99"), PriceEndings.charm(usd("12.99"), 99));
        },
        () -> {
          assertEquals(usd("12.99"), PriceEndings.charm(usd("12.30"), 99));
          assertEquals(usd("13.99"), PriceEndings.charm(usd("13.00"), 99));
        },
        () -> {
          assertThrows(IllegalArgumentException.class, () -> PriceEndings.charm(usd("1"), 100));
          assertThrows(IllegalArgumentException.class, () -> PriceEndings.charm(usd("1"), -1));
          assertThrows(IllegalArgumentException.class, () -> PriceEndings.charm(usd("-1"), 99));
        },
        () -> {
          assertEquals(usd("13.49"), PriceEndings.charm(usd("12.99"), 49));
        });
  }
}
