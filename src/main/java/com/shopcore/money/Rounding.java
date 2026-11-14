package com.shopcore.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Rounding helpers for amounts that are not yet whole minor units. */
public final class Rounding {

  private Rounding() {}

  /** Rounds a decimal amount in major units to the currency's minor unit. */
  public static Money round(BigDecimal amount, Currency currency, RoundingMode mode) {
    Objects.requireNonNull(amount, "amount");
    BigDecimal scaled = amount.setScale(currency.minorUnits(), mode);
    return Money.ofMinor(scaled.movePointRight(currency.minorUnits()).longValueExact(), currency);
  }

  /**
   * Rounds to a multiple of {@code incrementMinor} minor units, e.g. cash rounding to the nearest 5
   * paise ({@code 5}) or to whole rupees ({@code 100}).
   */
  public static Money roundToIncrement(Money amount, long incrementMinor, RoundingMode mode) {
    if (incrementMinor <= 0) {
      throw new IllegalArgumentException("increment must be positive");
    }
    BigDecimal steps =
        BigDecimal.valueOf(amount.minor()).divide(BigDecimal.valueOf(incrementMinor), 0, mode);
    return Money.ofMinor(steps.longValueExact() * incrementMinor, amount.currency());
  }
}
