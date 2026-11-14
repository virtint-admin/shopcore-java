// Issue 0041. Make this test pass by fixing the application code, not the test.
package com.shopcore.catalog;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class Issue0041Test {

  @Test
  void test_issue_0041() {
    assertAll(
        "issue 0041",
        () -> {
          assertEquals(
              Optional.of(CategoryPath.parse("Apparel > Men")),
              CategoryPath.parse("Apparel > Men > T-Shirts").parent());
        },
        () -> {
          assertEquals(
              "Apparel",
              CategoryPath.parse("Apparel > Men > T-Shirts")
                  .parent()
                  .flatMap(CategoryPath::parent)
                  .orElseThrow()
                  .toString());
        },
        () -> {
          assertEquals(Optional.empty(), CategoryPath.parse("Apparel").parent());
        });
  }
}
