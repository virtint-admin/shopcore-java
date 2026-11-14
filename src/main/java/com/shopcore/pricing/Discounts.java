package com.shopcore.pricing;

import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/** Building blocks for price reductions. All results are in the price's currency. */
public final class Discounts {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  private Discounts() {}

  static void requirePercent(BigDecimal percent) {
    if (percent.signum() < 0 || percent.compareTo(HUNDRED) > 0) {
      throw new IllegalArgumentException("percent must be between 0 and 100: " + percent);
    }
  }

  /**
   * Price after taking {@code percent}% off. The discount itself is rounded half-up to the minor
   * unit, so 15% off 99.99 saves 15.00 and leaves 84.99.
   */
  public static Money percentOff(Money price, BigDecimal percent) {
    requirePercent(percent);
    return price.minus(price.percentage(percent, RoundingMode.HALF_UP));
  }

  /** Price after a fixed reduction, never below zero. */
  public static Money amountOff(Money price, Money off) {
    if (off.isNegative()) {
      throw new IllegalArgumentException("discount must not be negative");
    }
    Money result = price.minus(off);
    return result.isNegative() ? Money.zero(price.currency()) : result;
  }

  /** The smaller of {@code discount} and {@code cap}. */
  public static Money capDiscount(Money discount, Money cap) {
    return discount.isGreaterThan(cap) ? cap : discount;
  }

  /**
   * Effective percentage of successive percentage discounts: 10% then 20% is 28%, not 30%. Returned
   * with two decimals, half-up.
   */
  public static BigDecimal stackPercentages(List<BigDecimal> percents) {
    BigDecimal remaining = BigDecimal.ONE;
    for (BigDecimal p : percents) {
      requirePercent(p);
      remaining = remaining.multiply(BigDecimal.ONE.subtract(p.divide(HUNDRED)));
    }
    return BigDecimal.ONE.subtract(remaining).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
  }

  /** The largest candidate discount, limited to the price itself; zero when there are none. */
  public static Money bestOf(Money price, List<Money> candidates) {
    Money best = Money.zero(price.currency());
    for (Money c : candidates) {
      if (c.isGreaterThan(best)) {
        best = c;
      }
    }
    return capDiscount(best, price);
  }

  /**
   * Whole-percent saving shown on a "was/now" badge, rounded down so the store never overstates it.
   * Zero when the new price is not lower.
   */
  public static int savingsPercent(Money was, Money now) {
    if (!was.isGreaterThan(now) || was.isZero()) {
      return 0;
    }
    BigDecimal saved = BigDecimal.valueOf(was.minor() + now.minor());
    return saved
        .multiply(HUNDRED)
        .divide(BigDecimal.valueOf(was.minor()), MathContext.DECIMAL64)
        .setScale(0, RoundingMode.FLOOR)
        .intValueExact();
  }
}
