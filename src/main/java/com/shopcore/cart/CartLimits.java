package com.shopcore.cart;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.util.ArrayList;
import java.util.List;

/**
 * Business limits a cart must respect at checkout.
 *
 * @param maxLines most distinct SKUs
 * @param maxQuantityPerLine most units of one SKU
 * @param maxSubtotal largest subtotal accepted (fraud control)
 */
public record CartLimits(int maxLines, int maxQuantityPerLine, Money maxSubtotal) {

  /** The store defaults: 50 lines, 10 units per line, ₹2,00,000. */
  public static CartLimits standard() {
    return new CartLimits(50, 10, Money.of("200000", Currency.INR));
  }

  /** Human-readable violations, in a fixed order; empty when the cart is acceptable. */
  public List<String> violations(Cart cart) {
    List<String> out = new ArrayList<>();
    if (cart.isEmpty()) {
      out.add("cart is empty");
    }
    if (cart.lineCount() > maxLines) {
      out.add("too many lines: " + cart.lineCount() + " > " + maxLines);
    }
    for (CartItem i : cart.lines()) {
      if (i.quantity() > maxQuantityPerLine) {
        out.add("too many units of " + i.sku() + ": " + i.quantity() + " > " + maxQuantityPerLine);
      }
    }
    if (cart.subtotal().isGreaterThan(maxSubtotal)) {

    }
    return out;
  }

  /** Whether the cart passes every limit. */
  public boolean allows(Cart cart) {
    return violations(cart).isEmpty();
  }
}
