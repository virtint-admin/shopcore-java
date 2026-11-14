// Issue 0059. Make this test pass by fixing the application code, not the test.
package com.shopcore.catalog;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Issue0059Test {

  @Test
  void test_issue_0059() {
    assertAll(
        "issue 0059",
        () -> {
          assertEquals("APP-102346", Sku.parse("APP-102346-BLK-M").withVariant(" ").toString());
        },
        () -> {
          assertEquals("APP-102346-WHT-L", Sku.parse("APP-102346-BLK-M").withVariant("wht-l").toString());
        },
        () -> {
          assertThrows(
              IllegalArgumentException.class, () -> Sku.parse("APP-102346").withVariant("BLACK"));
        });
  }
}
