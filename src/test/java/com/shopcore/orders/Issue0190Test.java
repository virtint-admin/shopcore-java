// Issue 0190. Make this test pass by fixing the application code, not the test.
package com.shopcore.orders;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class Issue0190Test {

  private static final LocalDate DAY = LocalDate.of(2026, 11, 9);

  @Test
  void test_issue_0190() {
    assertAll(
        "issue 0190",
        () -> {
          assertEquals(LocalDate.of(2028, 2, 29), OrderIds.dateOf("SC-20280229-00001"));
        },
        () -> {
          assertFalse(OrderIds.isValid(null));
          assertFalse(OrderIds.isValid("sc-20261109-00042"));
          assertFalse(OrderIds.isValid("SC-20261109-0042"));
          assertFalse(OrderIds.isValid("SC-20261109-00000"));
        },
        () -> {
          assertEquals(DAY, OrderIds.dateOf("SC-20261109-00042"));
        },
        () -> {
          assertThrows(IllegalArgumentException.class, () -> OrderIds.dateOf("SC-2026-1"));
        });
  }
}
