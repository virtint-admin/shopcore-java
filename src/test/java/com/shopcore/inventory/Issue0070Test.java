// Issue 0070. Make this test pass by fixing the application code, not the test.
package com.shopcore.inventory;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0070Test {

  @Test
  void test_issue_0070() {
    assertAll(
        "issue 0070",
        () -> {
          assertEquals(6, DemandForecast.daysOfCover(20, new BigDecimal("3.00")));
        },
        () -> {
          assertEquals(8, DemandForecast.daysOfCover(20, new BigDecimal("2.5")));
        },
        () -> {
          assertEquals(0, DemandForecast.daysOfCover(0, new BigDecimal("2.5")));
        },
        () -> {
          assertEquals(-1, DemandForecast.daysOfCover(20, BigDecimal.ZERO));
        });
  }
}
