// Issue 0285. Make this test pass by fixing the application code, not the test.
package com.shopcore.shipping;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Issue0285Test {

  private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
  private static final LocalDate HOLIDAY = LocalDate.of(2026, 11, 10); // Tuesday
  private static final DeliveryEstimator EST =
      new DeliveryEstimator(Set.of(HOLIDAY), LocalTime.of(14, 0));

  private static LocalDate day(int d) {
    return LocalDate.of(2026, 11, d);
  }

  @Test
  void test_issue_0285() {
    assertAll(
        "issue 0285",
        () -> {
          assertEquals(day(11), EST.addBusinessDays(day(9), 1));
          assertEquals(day(11), EST.addBusinessDays(day(6), 3));
        },
        () -> {
          assertEquals(day(7), EST.addBusinessDays(day(6), 1));
          assertEquals(day(9), EST.addBusinessDays(day(7), 1));
        },
        () -> {
          assertEquals(day(11), EST.dispatchDate(Instant.parse("2026-11-09T08:30:00Z"), IST));
        },
        () -> {
          // Sunday 20:00 UTC is Monday 01:30 in India.
          assertEquals(day(9), EST.dispatchDate(Instant.parse("2026-11-08T20:00:00Z"), IST));
          assertEquals(day(9), EST.dispatchDate(Instant.parse("2026-11-08T04:30:00Z"), IST));
        },
        () -> {
          assertEquals(
              day(11), EST.estimateDelivery(Instant.parse("2026-11-09T08:00:00Z"), IST, Zone.LOCAL));
        },
        () -> {
          assertEquals(
              day(18), EST.estimateDelivery(Instant.parse("2026-11-09T08:00:00Z"), IST, Zone.REMOTE));
          assertEquals(
              day(19), EST.estimateDelivery(Instant.parse("2026-11-09T09:00:00Z"), IST, Zone.REMOTE));
        });
  }
}
