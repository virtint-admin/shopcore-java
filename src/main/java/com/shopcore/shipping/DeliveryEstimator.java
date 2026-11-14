package com.shopcore.shipping;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * Delivery promises. Couriers work Monday to Saturday except listed holidays; orders placed before
 * the warehouse cut-off on a working day are dispatched the same day.
 */
public final class DeliveryEstimator {

  private final Set<LocalDate> holidays;
  private final LocalTime cutoff;

  /** An estimator with the given holiday calendar and same-day dispatch cut-off. */
  public DeliveryEstimator(Set<LocalDate> holidays, LocalTime cutoff) {
    this.holidays = Set.copyOf(holidays);
    this.cutoff = cutoff;
  }

  /** Whether couriers work on {@code date}. */
  public boolean isBusinessDay(LocalDate date) {
    return date.getDayOfWeek() != DayOfWeek.SUNDAY && !holidays.contains(date);
  }

  /** The {@code days}-th business day after {@code start} ({@code start} itself never counts). */
  public LocalDate addBusinessDays(LocalDate start, int days) {
    if (days < 0) {
      throw new IllegalArgumentException("days must not be negative");
    }
    LocalDate d = start;
    int added = 1;
    while (added < days) {
      d = d.plusDays(1);
      if (isBusinessDay(d)) {
        added++;
      }
    }
    return d;
  }

  /**
   * Dispatch date for an order placed at {@code orderedAt}, judged in the warehouse's time zone:
   * the same day if it is a business day and the order beat the cut-off, otherwise the next
   * business day.
   */
  public LocalDate dispatchDate(Instant orderedAt, ZoneId warehouseZone) {
    ZonedDateTime local = orderedAt.atZone(warehouseZone);
    LocalDate day = local.toLocalDate();
    if (isBusinessDay(day) && local.toLocalTime().isBefore(cutoff)) {
      return day;
    }
    return addBusinessDays(day, 1);
  }

  /** Promised delivery date: dispatch date plus the zone's transit days. */
  public LocalDate estimateDelivery(Instant orderedAt, ZoneId warehouseZone, Zone zone) {
    return addBusinessDays(dispatchDate(orderedAt, warehouseZone), zone.transitDays());
  }
}
