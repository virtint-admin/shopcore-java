// Issue 0047. Make this test pass by fixing the application code, not the test.
package com.shopcore.catalog;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Issue0047Test {

  @Test
  void test_issue_0047() {
    assertAll(
        "issue 0047",
        () -> {
          assertEquals(320, new Dimensions(100, 80, 40).volumeCm3());
        },
        () -> {
          assertEquals(8_000_000_000L, new Dimensions(20_000, 20_000, 20_000).volumeCm3());
        },
        () -> {
          assertEquals(1, new Dimensions(1, 1, 1).volumeCm3());
        },
        () -> {
          assertThrows(IllegalArgumentException.class, () -> new Dimensions(0, 1, 1).volumeCm3());
        });
  }
}
