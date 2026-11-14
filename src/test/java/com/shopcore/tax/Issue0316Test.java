// Issue 0316. Make this test pass by fixing the application code, not the test.
package com.shopcore.tax;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Issue0316Test {

  private static final Region KA = Region.parse("IN-KA");

  private static TaxRule rule(String region, String category) {
    return new TaxRule(region, category, new BigDecimal("18"), true);
  }

  @Test
  void test_issue_0316() {
    assertAll(
        "issue 0316",
        () -> {
          assertEquals(20, rule("IN", "Grocery > Staples").specificity());
          assertEquals(11, rule("IN-KL", "Books").specificity());
        },
        () -> {
          assertEquals(1, rule("IN-KL", "*").specificity());
        },
        () -> {
          assertEquals(0, rule("IN", "*").specificity());
        });
  }
}
