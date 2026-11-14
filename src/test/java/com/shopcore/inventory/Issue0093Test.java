// Issue 0093. Make this test pass by fixing the application code, not the test.
package com.shopcore.inventory;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class Issue0093Test {

  private static final Instant EXPIRES = Instant.parse("2026-03-01T10:15:00Z");
  private static final Reservation R =
      new Reservation("RSV-0001", "ELE-555012", "BLR1", 2, EXPIRES);

  @Test
  void test_issue_0093() {
    assertAll(
        "issue 0093",
        () -> {
          assertEquals(900, R.secondsRemaining(Instant.parse("2026-03-01T10:00:00Z")));
        },
        () -> {
          assertEquals(0, R.secondsRemaining(EXPIRES.plusSeconds(3600)));
        },
        () -> {
          assertEquals(0, R.secondsRemaining(EXPIRES));
        });
  }
}
