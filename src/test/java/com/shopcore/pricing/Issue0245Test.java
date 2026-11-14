// Issue 0245. Make this test pass by fixing the application code, not the test.
package com.shopcore.pricing;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import org.junit.jupiter.api.Test;

class Issue0245Test {

  private static Money usd(String a) {
    return Money.of(a, Currency.USD);
  }

  @Test
  void test_issue_0245() {
    assertAll(
        "issue 0245",
        () -> {
          assertEquals(
              Money.of("489", Currency.INR), PriceEndings.endInDigit(Money.of("489", Currency.INR), 9));
          assertEquals(
              Money.of("499", Currency.INR),
              PriceEndings.endInDigit(Money.of("489.01", Currency.INR), 9));
        },
        () -> {
          assertEquals(
              Money.of("489", Currency.INR),
              PriceEndings.endInDigit(Money.of("481.50", Currency.INR), 9));
        },
        () -> {
          assertThrows(IllegalArgumentException.class, () -> PriceEndings.endInDigit(usd("1"), 10));
        },
        () -> {
          assertEquals(
              Money.of("1240", Currency.JPY), PriceEndings.endInDigit(Money.of("1234", Currency.JPY), 0));
        });
  }
}
