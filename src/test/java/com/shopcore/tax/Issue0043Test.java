// Issue 0043. Make this test pass by fixing the application code, not the test.
package com.shopcore.tax;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class Issue0043Test {

  private static final TaxTable STD = TaxTable.standard();
  private static final Region KA = Region.parse("IN-KA");
  private static final Region KL = Region.parse("IN-KL");

  private static BigDecimal pct(String p) {
    return new BigDecimal(p);
  }

  @Test
  void test_issue_0043() {
    assertAll(
        "issue 0043",
        () -> {
          assertEquals("Books", STD.ruleFor(KL, "Books > Business").orElseThrow().category());
        },
        () -> {
          assertEquals(
              "Grocery > Staples", STD.ruleFor(KA, "Grocery > Staples").orElseThrow().category());
        },
        () -> {
          assertEquals(Optional.empty(), STD.ruleFor(Region.parse("FR"), "Books"));
        });
  }
}
