// Issue 0087. Make this test pass by fixing the application code, not the test.
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

class Issue0087Test {

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
  void test_issue_0087() {
    assertAll(
        "issue 0087",
        () -> {
          Inventory inv = stocked(10, 0);
          assertEquals(8, inv.adjust("BLR1", SKU, -2, "damaged", T0).onHand());
        },
        () -> {
          Inventory inv = stocked(10, 0);
          inv.reserve("BLR1", SKU, 6, T0, HOLD);
          assertThrows(InsufficientStockException.class, () -> inv.adjust("BLR1", SKU, -5, "lost", T0));
          assertEquals(6, inv.adjust("BLR1", SKU, -4, "lost", T0).onHand());
        },
        () -> {
          Inventory inv = stocked(10, 0);
          inv.adjust("BLR1", SKU, 3, " found in recount ", T0);
          Movement m = inv.movementsFor(SKU).get(1);
          assertEquals(MovementType.ADJUST, m.type());
          assertEquals("found in recount", m.note());
          assertEquals(3, m.delta());
        },
        () -> {
          Inventory inv = stocked(10, 0);
          assertThrows(IllegalArgumentException.class, () -> inv.adjust("BLR1", SKU, -1, " ", T0));
          assertThrows(IllegalArgumentException.class, () -> inv.adjust("BLR1", SKU, -1, null, T0));
          assertThrows(IllegalArgumentException.class, () -> inv.adjust("BLR1", SKU, 0, "x", T0));
        });
  }
}
