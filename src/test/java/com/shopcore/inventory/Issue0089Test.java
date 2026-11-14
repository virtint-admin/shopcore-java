// Issue 0089. Make this test pass by fixing the application code, not the test.
package com.shopcore.inventory;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Issue0089Test {

  private static final ReorderPolicy POLICY = new ReorderPolicy(20, 50, 10);

  @Test
  void test_issue_0089() {
    assertAll(
        "issue 0089",
        () -> {
          assertEquals(45, ReorderPolicy.reorderPointFor(5.0, 7, 10));
        },
        () -> {
          assertEquals(11, ReorderPolicy.reorderPointFor(2.5, 3, 3));
        },
        () -> {
          assertThrows(IllegalArgumentException.class, () -> ReorderPolicy.reorderPointFor(-1, 3, 0));
          assertThrows(IllegalArgumentException.class, () -> ReorderPolicy.reorderPointFor(1, -3, 0));
          assertThrows(IllegalArgumentException.class, () -> ReorderPolicy.reorderPointFor(1, 3, -1));
        });
  }
}
