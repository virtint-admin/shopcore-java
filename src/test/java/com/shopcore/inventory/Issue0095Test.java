// Issue 0095. Make this test pass by fixing the application code, not the test.
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

class Issue0095Test {

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
  void test_issue_0095() {
    assertAll(
        "issue 0095",
        () -> {
          assertEquals(Optional.empty(), stocked(5, 4).bestWarehouse(SKU, 6));
        },
        () -> {
          Inventory inv = stocked(10, 0);
          inv.reserve("BLR1", SKU, 8, T0, HOLD);
          assertThrows(InsufficientStockException.class, () -> inv.transfer("BLR1", "DEL1", SKU, 3, T0));
          assertEquals(10, inv.stockOf("BLR1", SKU).onHand());
        },
        () -> {
          Inventory inv = stocked(10, 1);
          Transfer t = inv.transfer("BLR1", "DEL1", SKU, 4, T0);
          assertEquals(new Transfer("TRF-0001", "BLR1", "DEL1", SKU, 4, T0), t);
          assertEquals(6, inv.stockOf("BLR1", SKU).onHand());
          assertEquals(5, inv.stockOf("DEL1", SKU).onHand());
        },
        () -> {
          Inventory inv = stocked(10, 0);
          assertThrows(IllegalArgumentException.class, () -> inv.transfer("BLR1", "BLR1", SKU, 1, T0));
          assertThrows(IllegalArgumentException.class, () -> inv.transfer("BLR1", "DEL1", SKU, 0, T0));
        });
  }
}
