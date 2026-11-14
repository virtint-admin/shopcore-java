// Issue 0169. Make this test pass by fixing the application code, not the test.
package com.shopcore.money;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0169Test {

  private static Money inr(String amount) {
    return Money.of(amount, Currency.INR);
  }

  @Test
  void test_issue_0169() {
    assertAll(
        "issue 0169",
        () -> {
          assertEquals(
              List.of(inr("33.34"), inr("33.33"), inr("33.33")), inr("100.00").allocate(1, 1, 1));
        },
        () -> {
          assertEquals(List.of(inr("0.00"), inr("0.02"), inr("0.01")), inr("0.03").allocate(0, 1, 1));
        },
        () -> {
          assertEquals(List.of(inr("0.03"), inr("0.07")), inr("0.10").allocate(30, 70));
        },
        () -> {
          assertThrows(IllegalArgumentException.class, () -> inr("1").allocate());
          assertThrows(IllegalArgumentException.class, () -> inr("1").allocate(0, 0));
          assertThrows(IllegalArgumentException.class, () -> inr("1").allocate(1, -1));
        });
  }
}
