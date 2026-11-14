// Issue 0062. Make this test pass by fixing the application code, not the test.
package com.shopcore.inventory;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0062Test {

  @Test
  void test_issue_0062() {
    assertAll(
        "issue 0062",
        () -> {
          assertEquals(new BigDecimal("3.67"), DemandForecast.averageDaily(List.of(3, 4, 4)));
        },
        () -> {
          assertEquals(
              new BigDecimal("0.13"), DemandForecast.averageDaily(List.of(1, 0, 0, 0, 0, 0, 0, 0)));
        },
        () -> {
          assertEquals(
              List.of(new BigDecimal("2.00"), new BigDecimal("3.00"), new BigDecimal("4.00")),
              DemandForecast.movingAverage(List.of(1, 2, 3, 4, 5), 3));
        },
        () -> {
          assertEquals(List.of(new BigDecimal("1.50")), DemandForecast.movingAverage(List.of(1, 2), 2));
        },
        () -> {
          assertThrows(IllegalArgumentException.class, () -> DemandForecast.movingAverage(List.of(1), 0));
        },
        () -> {
          assertEquals(List.of(), DemandForecast.movingAverage(List.of(1, 2), 3));
        });
  }
}
