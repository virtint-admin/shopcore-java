// Issue 0097. Make this test pass by fixing the application code, not the test.
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

class Issue0097Test {

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
  void test_issue_0097() {
    assertAll(
        "issue 0097",
        () -> {
          Inventory inv = stocked(10, 0);
          inv.reserve("BLR1", SKU, 2, T0, HOLD);
          assertEquals(1, inv.expireReservations(T0.plus(HOLD)));
          assertEquals(10, inv.stockOf("BLR1", SKU).available());
        },
        () -> {
          Inventory inv = stocked(5, 0);
          Reservation r = inv.reserve("BLR1", SKU, 4, T0, HOLD);
          assertTrue(inv.release(r.id()));
          assertEquals(5, inv.stockOf("BLR1", SKU).available());
        },
        () -> {
          Inventory inv = stocked(5, 0);
          Reservation r = inv.reserve("BLR1", SKU, 4, T0, HOLD);
          inv.release(r.id());
          assertFalse(inv.release(r.id()));
          assertEquals(new StockLevel("BLR1", SKU, 5, 0), inv.stockOf("BLR1", SKU));
        },
        () -> {
          assertFalse(new Inventory().release("RSV-0001"));
        });
  }
}
