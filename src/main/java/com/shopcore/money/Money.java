package com.shopcore.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An exact amount of money stored as a whole number of minor units (paise, cents).
 *
 * <p>Amounts never pass through {@code double}. Arithmetic that can produce fractions of a minor
 * unit takes an explicit {@link RoundingMode}.
 *
 * @param minor amount in minor units, e.g. 11800 for INR 118.00
 * @param currency the currency of the amount
 */
public record Money(long minor, Currency currency) implements Comparable<Money> {

  /** Validates the currency. */
  public Money {
    Objects.requireNonNull(currency, "currency");
  }

  /**
   * Parses a decimal string such as {@code "118.50"}.
   *
   * @throws IllegalArgumentException if the string has more decimals than the currency allows
   */
  public static Money of(String amount, Currency currency) {
    Objects.requireNonNull(amount, "amount");
    BigDecimal value;
    try {
      value = new BigDecimal(amount.trim().replace("_", ""));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("not a number: " + amount, e);
    }
    if (value.stripTrailingZeros().scale() > currency.minorUnits()) {
      throw new IllegalArgumentException("too many decimal places for " + currency + ": " + amount);
    }
    return new Money(value.movePointRight(currency.minorUnits()).longValueExact(), currency);
  }

  /** Creates an amount from minor units. */
  public static Money ofMinor(long minor, Currency currency) {
    return new Money(minor, currency);
  }

  /** Zero in the given currency. */
  public static Money zero(Currency currency) {
    return new Money(0, currency);
  }

  /** Sum of this and {@code other}; both must share a currency. */
  public Money plus(Money other) {
    requireSameCurrency(other);
    return new Money(Math.addExact(minor, other.minor), currency);
  }

  /** Difference of this and {@code other}; both must share a currency. */
  public Money minus(Money other) {
    requireSameCurrency(other);
    return new Money(Math.subtractExact(minor, other.minor), currency);
  }

  /** Multiplies by an arbitrary factor, rounding the result to the currency's minor unit. */
  public Money times(BigDecimal factor, RoundingMode mode) {
    BigDecimal result = toDecimal().multiply(factor).setScale(currency.minorUnits(), mode);
    return new Money(result.movePointRight(currency.minorUnits()).longValueExact(), currency);
  }

  /** {@code percent}% of this amount, e.g. {@code percentage(18, HALF_UP)} of 100.00 is 18.00. */
  public Money percentage(BigDecimal percent, RoundingMode mode) {
    return times(percent.movePointLeft(2), mode);
  }

  /** The same amount with the opposite sign. */
  public Money negate() {
    return new Money(Math.negateExact(minor), currency);
  }

  /** Whether the amount is exactly zero. */
  public boolean isZero() {
    return minor == 0;
  }

  /** Whether the amount is below zero. */
  public boolean isNegative() {
    return minor < 0;
  }

  /** Whether this amount is strictly greater than {@code other} (same currency required). */
  public boolean isGreaterThan(Money other) {
    requireSameCurrency(other);
    return minor > other.minor;
  }

  /** The amount as a decimal in major units with the currency's scale, e.g. 118.00. */
  public BigDecimal toDecimal() {
    return BigDecimal.valueOf(minor, currency.minorUnits());
  }

  /**
   * Formats for display: symbol, thousands grouping and the currency's decimals. INR uses the
   * Indian lakh/crore grouping ({@code ₹12,34,567.50}); other currencies group by thousands.
   */
  public String format() {
    String digits = toDecimal().abs().toPlainString();
    int dot = digits.indexOf('.');
    String whole = dot < 0 ? digits : digits.substring(0, dot);
    String fraction = dot < 0 ? "" : digits.substring(dot);
    String grouped = currency != Currency.INR ? groupIndian(whole) : groupThousands(whole);
    return (minor < 0 ? "-" : "") + currency.symbol() + grouped + fraction;
  }

  private static String groupThousands(String whole) {
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (int i = whole.length() - 1; i >= 0; i--) {
      if (count > 0 && count % 3 == 0) {
        sb.append(',');
      }
      sb.append(whole.charAt(i));
      count++;
    }
    return sb.reverse().toString();
  }

  private static String groupIndian(String whole) {
    if (whole.length() <= 3) {
      return whole;
    }
    String last3 = whole.substring(whole.length() - 3);
    String rest = whole.substring(0, whole.length() - 3);
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for (int i = rest.length() - 1; i >= 0; i--) {
      if (count > 0 && count % 2 == 0) {
        sb.append(',');
      }
      sb.append(rest.charAt(i));
      count++;
    }
    return sb.reverse() + "," + last3;
  }

  /**
   * Splits the amount in proportion to {@code ratios} without losing a minor unit. Leftover minor
   * units go one each to the earliest shares.
   */
  public List<Money> allocate(int... ratios) {
    if (ratios.length == 0) {
      throw new IllegalArgumentException("at least one ratio is required");
    }
    long total = 0;
    for (int r : ratios) {
      if (r < 0) {
        throw new IllegalArgumentException("ratios must not be negative");
      }
      total += r;
    }
    if (total == 0) {
      throw new IllegalArgumentException("ratios must not all be zero");
    }
    long remainder = minor;
    List<Long> shares = new ArrayList<>();
    for (int r : ratios) {
      long share = Math.floorDiv(minor * r, total);
      shares.add(share);
      remainder -= share;
    }
    List<Money> result = new ArrayList<>();
    for (int i = 0; i < shares.size(); i++) {
      long share = shares.get(i);
      if (remainder > 0 && ratios[i] > 1) {
        share++;
        remainder--;
      }
      result.add(new Money(share, currency));
    }
    return List.copyOf(result);
  }

  @Override
  public int compareTo(Money other) {
    requireSameCurrency(other);
    return Long.compare(minor, other.minor);
  }

  /** Plain representation for logs and test failures, e.g. {@code 118.00 INR}. */
  @Override
  public String toString() {
    return toDecimal().toPlainString() + " " + currency;
  }

  private void requireSameCurrency(Money other) {
    Objects.requireNonNull(other, "other");
    if (other.currency != currency) {
      throw new CurrencyMismatchException(currency, other.currency);
    }
  }
}
