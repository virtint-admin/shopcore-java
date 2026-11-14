package com.shopcore.orders;

import com.shopcore.money.Money;

/**
 * A purchased line.
 *
 * @param sku SKU text
 * @param unitPrice price paid per unit
 * @param quantity units bought
 */
public record OrderLine(String sku, Money unitPrice, int quantity) {

  /** Validates the line. */
  public OrderLine {
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be positive");
    }
    if (unitPrice.isNegative()) {
      throw new IllegalArgumentException("unit price must not be negative");
    }
  }

  /** Unit price times quantity. */
  public Money lineTotal() {
    return Money.ofMinor(Math.multiplyExact(unitPrice.minor(), quantity), unitPrice.currency());
  }
}
