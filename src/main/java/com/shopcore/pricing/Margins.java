package com.shopcore.pricing;

import com.shopcore.money.Money;
import com.shopcore.money.Rounding;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Margin and markup arithmetic used by merchandising. */
public final class Margins {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  private Margins() {}

  /** Gross margin as a percent of the selling price, two decimals half-up. */
  public static BigDecimal marginPercent(Money cost, Money price) {
    if (price.minor() <= 0) {
      throw new IllegalArgumentException("price must be positive");
    }
    BigDecimal profit = BigDecimal.valueOf(price.minus(cost).minor());
    return profit
        .multiply(HUNDRED)
        .divide(BigDecimal.valueOf(price.minor()), 2, RoundingMode.HALF_UP);
  }

  /** Markup as a percent of cost, two decimals half-up. */
  public static BigDecimal markupPercent(Money cost, Money price) {
    if (cost.minor() <= 0) {
      throw new IllegalArgumentException("cost must be positive");
    }
    BigDecimal profit = BigDecimal.valueOf(price.minus(cost).minor());
    return profit
        .multiply(HUNDRED)
        .divide(BigDecimal.valueOf(cost.minor()), 2, RoundingMode.HALF_UP);
  }

  /**
   * Lowest price that achieves at least {@code marginPercent}: {@code cost / (1 - m/100)}, rounded
   * up to the minor unit.
   */
  public static Money priceForMargin(Money cost, BigDecimal marginPercent) {
    if (marginPercent.signum() < 0 || marginPercent.compareTo(HUNDRED) >= 0) {
      throw new IllegalArgumentException("margin must be in [0, 100)");
    }
    BigDecimal keep = BigDecimal.ONE.subtract(marginPercent.divide(HUNDRED));
    BigDecimal price = cost.toDecimal().divide(keep, MathContext.DECIMAL64);
    return Rounding.round(price, cost.currency(), RoundingMode.CEILING);
  }

  /** Cost plus {@code markupPercent}, rounded half-up. */
  public static Money priceForMarkup(Money cost, BigDecimal markupPercent) {
    if (markupPercent.signum() < 0) {
      throw new IllegalArgumentException("markup must not be negative");
    }
    return cost.plus(cost.percentage(markupPercent, RoundingMode.HALF_UP));
  }

  /** Whether selling at {@code price} loses money. Selling exactly at cost is not a loss. */
  public static boolean isBelowCost(Money cost, Money price) {
    return cost.isGreaterThan(price);
  }
}
