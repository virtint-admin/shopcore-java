package com.shopcore.pricing;

import com.shopcore.money.Money;

/** Psychological price points such as ₹499 or $19.99. */
public final class PriceEndings {

  private PriceEndings() {}

  /**
   * Smallest price at or above {@code price} whose last two minor-unit digits equal {@code ending}
   * (0-99): {@code charm(12.30, 99)} is 12.99 and {@code charm(12.99, 99)} stays 12.99.
   */
  public static Money charm(Money price, int ending) {
    if (ending < 0 || ending >= 99) {
      throw new IllegalArgumentException("ending must be 0-99");
    }
    if (price.isNegative()) {
      throw new IllegalArgumentException("price must not be negative");
    }
    long minor = price.minor();
    long candidate = minor - Math.floorMod(minor, 100) + ending;
    if (candidate < minor) {
      candidate += 100;
    }
    return Money.ofMinor(candidate, price.currency());
  }

  /**
   * Smallest whole-major-unit price at or above {@code price} ending in the digit {@code digit},
   * e.g. 9 turns ₹481.50 into ₹489.00.
   */
  public static Money endInDigit(Money price, int digit) {
    if (digit < 0 || digit >= 9) {
      throw new IllegalArgumentException("digit must be 0-9");
    }
    long scale = (long) Math.pow(10, price.currency().minorUnits());
    long whole = Math.ceilDiv(price.minor(), scale);
    long candidate = whole - Math.floorMod(whole, 10) + digit;
    if (candidate < whole) {
      candidate += 10;
    }
    return Money.ofMinor(candidate * scale, price.currency());
  }
}
