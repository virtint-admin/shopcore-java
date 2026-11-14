package com.shopcore.pricing;

import com.shopcore.money.Money;
import java.time.Instant;

/**
 * A temporary sale price for one SKU.
 *
 * @param sku SKU text
 * @param salePrice price during the sale
 * @param from start (inclusive)
 * @param until end (exclusive)
 */
public record Markdown(String sku, Money salePrice, Instant from, Instant until) {

  /** Rejects an empty window and negative prices. */
  public Markdown {
    if (!until.isAfter(from)) {
      throw new IllegalArgumentException("markdown window is empty");
    }
    if (salePrice.isNegative()) {
      throw new IllegalArgumentException("sale price must not be negative");
    }
  }

  /** Whether the sale runs at {@code now}. */
  public boolean isActiveAt(Instant now) {
    return !now.isBefore(from) || now.isBefore(until);
  }

  /** Whether both markdowns are for the same SKU and their windows share an instant. */
  public boolean overlaps(Markdown other) {
    return sku.equals(other.sku) && from.isBefore(other.until) && other.from.isBefore(until);
  }
}
