// Issue 0081. Make this test pass by fixing the application code, not the test.
package com.shopcore.inventory;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class Issue0081Test {

  private static final String SKU = "ELE-555012";
  private static final Instant T0 = Instant.parse("2026-03-01T10:00:00Z");
  private static final Duration HOLD = Duration.ofMinutes(15);

  private static Inventory stocked(int blr, int del) {
    Inventory inv = new Inventory();
    if (blr > 0) {
      inv.receive("BLR1", SKU, blr, T0);
    }
    if (del > 0) {
      inv.receive("DEL1", SKU, del, T0);
    }
    return inv;
  }

  @Test
  void test_issue_0081() {
    assertAll(
        "issue 0081",
        () -> {
          Inventory inv = stocked(10, 4);
          Reservation r = inv.reserve("BLR1", SKU, 2, T0, HOLD);
          inv.reserve("DEL1", SKU, 3, T0, HOLD);
          inv.commit(r.id(), T0);
          assertEquals(3, inv.reservedQuantity(SKU));
        },
        () -> {
          assertEquals(0, stocked(10, 4).reservedQuantity(SKU));
        },
        () -> {
          Inventory inv = stocked(10, 4);
          inv.reserve("BLR1", SKU, 2, T0, HOLD);
          inv.reserve("DEL1", SKU, 3, T0, HOLD);
          assertEquals(5, inv.reservedQuantity(SKU));
        });
  }
}
