// Issue 0248. Make this test pass by fixing the application code, not the test.
package com.shopcore.pricing;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class Issue0248Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static final PriceTier T1 = new PriceTier(1, inr("100"));
  private static final PriceTier T10 = new PriceTier(10, inr("90"));
  private static final PriceTier T50 = new PriceTier(50, inr("80"));
  private static final TieredPricing P = new TieredPricing(List.of(T1, T10, T50));

  @Test
  void test_issue_0248() {
    assertAll(
        "issue 0248",
        () -> {
          assertEquals(T10, P.tierFor(10));
          assertEquals(T50, P.tierFor(50));
        },
        () -> {
          assertEquals(inr("4000"), P.totalFor(50));
        },
        () -> {
          assertEquals(inr("900"), P.totalFor(10));
        },
        () -> {
          assertEquals(inr("900"), P.totalFor(9));
        });
  }
}
