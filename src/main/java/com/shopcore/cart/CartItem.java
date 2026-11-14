package com.shopcore.cart;

import com.shopcore.money.Money;
import java.util.Objects;

/**
 * One line in a cart.
 *
 * @param sku canonical SKU text
 * @param unitPrice price per unit when the line was last updated
 * @param quantity units, at least 1
 */
public record CartItem(String sku, Money unitPrice, int quantity) {

  /** Validates the line. */
  public CartItem {
    Objects.requireNonNull(sku, "sku");
    Objects.requireNonNull(unitPrice, "unitPrice");
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

  /** The same line with a different quantity. */
  public CartItem withQuantity(int newQuantity) {
    return new CartItem(sku, unitPrice, newQuantity);
  }
}
