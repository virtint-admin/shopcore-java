// Issue 0072. Make this test pass by fixing the application code, not the test.
package com.shopcore.inventory;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Issue0072Test {

  @Test
  void test_issue_0072() {
    assertAll(
        "issue 0072",
        () -> {
          InsufficientStockException e = new InsufficientStockException("ELE-555012", 5, 2);
          assertEquals("insufficient stock for ELE-555012: requested 5, available 2", e.getMessage());
          assertEquals(3, e.shortfall());
        },
        () -> {
          assertEquals(5, new InsufficientStockException("ELE-555012", 5, -4).shortfall());
        },
        () -> {
          assertEquals(3, new InsufficientStockException("ELE-555012", 5, 2).shortfall());
        });
  }
}
