// Issue 0084. Make this test pass by fixing the application code, not the test.
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

class Issue0084Test {

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
  void test_issue_0084() {
    assertAll(
        "issue 0084",
        () -> {
          assertEquals(
              Map.of(), stocked(30, 0).reorderSuggestions(Map.of(SKU, new ReorderPolicy(20, 5, 0))));
        },
        () -> {
          Inventory inv = stocked(10, 4);
          inv.receive("BLR1", "BOK-000000", 100, T0);
          Map<String, Integer> out =
              inv.reorderSuggestions(
                  Map.of(
                      SKU, new ReorderPolicy(20, 50, 10), "BOK-000000", new ReorderPolicy(20, 50, 10)));
          assertEquals(Map.of(SKU, 50), out);
        },
        () -> {
          Map<String, Integer> out =
              new Inventory().reorderSuggestions(Map.of("TOY-200028", new ReorderPolicy(5, 10, 20)));
          assertEquals(Map.of("TOY-200028", 25), out);
        });
  }
}
