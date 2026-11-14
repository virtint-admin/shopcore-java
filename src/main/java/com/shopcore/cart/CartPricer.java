package com.shopcore.cart;

import com.shopcore.money.Money;
import com.shopcore.tax.TaxCalculator;
import java.math.BigDecimal;

/** Turns a cart into checkout totals. */
public final class CartPricer {

  private CartPricer() {}

  /**
   * Prices a cart. The coupon discount comes off merchandise before tax; tax is never charged on
   * shipping. With tax-inclusive prices the tax is extracted from the discounted merchandise,
   * otherwise it is added on top. A free-shipping coupon waives {@code shippingFee}.
   */
  public static CartTotals price(
      Cart cart, BigDecimal taxRatePercent, boolean pricesIncludeTax, Money shippingFee) {
    Money subtotal = cart.subtotal();
    Money discount = cart.discount();
    Money merchandise = subtotal.minus(discount);
    boolean waived = cart.coupon().map(c -> c.waivesShipping()).orElse(false) && !cart.isEmpty();
    Money shipping = waived || cart.isEmpty() ? Money.zero(cart.currency()) : shippingFee;
    Money tax;
    Money total;
    if (pricesIncludeTax) {
      tax = TaxCalculator.taxInGross(merchandise, taxRatePercent);
      total = merchandise.plus(shipping);
    } else {
      tax = TaxCalculator.taxOnNet(merchandise, taxRatePercent);
      total = merchandise.plus(tax).plus(shipping);
    }
    return new CartTotals(subtotal, discount, shipping, tax, total);
  }

  /** How much more merchandise qualifies the cart for free shipping; zero once reached. */
  public static Money freeShippingGap(Cart cart, Money threshold) {
    Money gap = threshold.minus(cart.subtotal().minus(cart.discount()));
    return gap.isNegative() ? Money.zero(gap.currency()) : gap;
  }
}
