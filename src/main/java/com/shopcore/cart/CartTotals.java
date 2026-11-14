package com.shopcore.cart;

import com.shopcore.money.Money;

/**
 * Checkout summary.
 *
 * @param subtotal sum of lines
 * @param discount coupon discount on merchandise
 * @param shipping shipping charged (zero when waived)
 * @param tax tax included in or added to the merchandise
 * @param total amount to pay
 */
public record CartTotals(Money subtotal, Money discount, Money shipping, Money tax, Money total) {

  /** Merchandise value after the discount. */
  public Money merchandiseAfterDiscount() {
    return subtotal.minus(discount);
  }

  /** Whether the shopper pays nothing. */
  public boolean isFree() {
    return total.isZero();
  }
}
