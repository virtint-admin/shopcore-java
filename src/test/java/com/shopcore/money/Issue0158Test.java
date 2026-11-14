// Issue 0158. Make this test pass by fixing the application code, not the test.
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

class Issue0158Test {

  private static Money inr(String amount) {
    return Money.of(amount, Currency.INR);
  }

  @Test
  void test_issue_0158() {
    assertAll(
        "issue 0158",
        () -> {
          assertEquals("₹12,34,567.50", inr("1234567.50").format());
        },
        () -> {
          assertEquals("$1,234,567.50", Money.of("1234567.5", Currency.USD).format());
        },
        () -> {
          assertEquals("₹1,000.00", inr("1000").format());
        },
        () -> {
          assertEquals("₹999.00", inr("999").format());
        });
  }
}
