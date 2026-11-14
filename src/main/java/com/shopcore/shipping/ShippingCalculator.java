package com.shopcore.shipping;

import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Shipping charges shown at checkout. */
public final class ShippingCalculator {

  private ShippingCalculator() {}

  /**
   * Shipping charged to the customer: free when the merchandise subtotal reaches {@code
   * freeThreshold}, otherwise the courier rate for the chargeable weight.
   */
  public static Money quote(
      RateTable rates, Zone zone, int chargeableGrams, Money subtotal, Money freeThreshold) {
    if (subtotal.compareTo(freeThreshold) >= 0) {
      return Money.zero(subtotal.currency());
    }
    return rates.rateFor(zone, chargeableGrams);
  }

  /** Cash-on-delivery handling fee: 2% of the order total (half-up), at least 40.00. */
  public static Money codFee(Money orderTotal) {
    Money pct = orderTotal.percentage(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
    Money min = Money.of("40", orderTotal.currency());
    return pct.isGreaterThan(min) ? pct : min;
  }
}
