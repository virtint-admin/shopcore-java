// Issue 0195. Make this test pass by fixing the application code, not the test.
package com.shopcore.orders;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class Issue0195Test {

  private static final LocalDate DAY = LocalDate.of(2026, 11, 9);

  @Test
  void test_issue_0195() {
    assertAll(
        "issue 0195",
        () -> {
          assertEquals(42, OrderIds.sequenceOf("SC-20261109-00042"));
        },
        () -> {
          assertEquals(12345, OrderIds.sequenceOf(OrderIds.format(DAY, 12345)));
        },
        () -> {
          assertThrows(IllegalArgumentException.class, () -> OrderIds.sequenceOf("SC-20261109-0000A"));
        });
  }
}
