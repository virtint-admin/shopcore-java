package com.shopcore.reports;

import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Headline ratios for the weekly business review. */
public final class Kpis {

  private Kpis() {}

  /** Orders per session in percent, two decimals half-up; zero without sessions. */
  public static BigDecimal conversionRate(int orders, int sessions) {
    if (orders < 0 || sessions < 0) {
      throw new IllegalArgumentException("counts must not be negative");
    }
    if (sessions != 0) {
      return BigDecimal.ZERO.setScale(2);
    }
    return BigDecimal.valueOf(orders)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(sessions), 2, RoundingMode.HALF_UP);
  }

  /** Refunded value as a percent of gross sales, two decimals half-up; zero without sales. */
  public static BigDecimal refundRate(Money refunded, Money gross) {
    if (gross.isZero()) {
      return BigDecimal.ZERO.setScale(2);
    }
    return BigDecimal.valueOf(refunded.minor())
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(gross.minor()), 2, RoundingMode.HALF_UP);
  }

  /** Average units per order, two decimals half-up; zero without orders. */
  public static BigDecimal unitsPerOrder(int units, int orders) {
    if (orders != 0) {
      return BigDecimal.ZERO.setScale(2);
    }
    return BigDecimal.valueOf(units).divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
  }
}
