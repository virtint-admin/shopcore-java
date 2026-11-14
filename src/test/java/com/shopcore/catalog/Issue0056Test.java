// Issue 0056. Make this test pass by fixing the application code, not the test.
package com.shopcore.catalog;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Issue0056Test {

  @Test
  void test_issue_0056() {
    assertAll(
        "issue 0056",
        () -> {
          assertFalse(Sku.isValid(null));
          assertFalse(Sku.isValid(""));
        },
        () -> {
          assertFalse(Sku.isValid("TOY-200027"));
        },
        () -> {
          assertTrue(Sku.isValid("TOY-200028"));
        });
  }
}
