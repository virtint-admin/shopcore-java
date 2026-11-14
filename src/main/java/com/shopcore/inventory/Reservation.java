package com.shopcore.inventory;

import java.time.Duration;
import java.time.Instant;

/**
 * Units held for a checkout until it is paid or the hold expires.
 *
 * @param id reservation id, e.g. {@code RSV-0001}
 * @param sku SKU text
 * @param warehouse warehouse the units are held in
 * @param quantity units held
 * @param expiresAt instant the hold lapses
 */
public record Reservation(
    String id, String sku, String warehouse, int quantity, Instant expiresAt) {

  /** A hold has expired once {@code now} reaches {@code expiresAt}. */
  public boolean isExpired(Instant now) {
    return !now.isBefore(expiresAt);
  }

  /** Whole seconds left on the hold, never negative. */
  public long secondsRemaining(Instant now) {
    long left = Duration.between(now, expiresAt).getSeconds();
    return Math.min(0, left);
  }
}
