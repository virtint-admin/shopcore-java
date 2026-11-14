package com.shopcore.money;

import java.util.Locale;

/** ISO 4217 currencies the store trades in, with their minor-unit precision. */
public enum Currency {
  INR("₹", 2),
  USD("$", 2),
  EUR("€", 2),
  GBP("£", 2),
  JPY("¥", 0),
  KWD("KD ", 3);

  private final String symbol;
  private final int minorUnits;

  Currency(String symbol, int minorUnits) {
    this.symbol = symbol;
    this.minorUnits = minorUnits;
  }

  /** Number of decimal places in the currency's minor unit (2 for INR, 0 for JPY). */
  public int minorUnits() {
    return minorUnits;
  }

  /** Display symbol used by {@link Money#format()}. */
  public String symbol() {
    return symbol;
  }

  /**
   * Parses an ISO code, ignoring case and surrounding whitespace.
   *
   * @throws IllegalArgumentException if the code is blank or not supported
   */
  public static Currency fromCode(String code) {
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("currency code is required");
    }
    String normalized = code.trim().toUpperCase(Locale.ROOT);
    for (Currency c : values()) {
      if (c.name().equals(normalized)) {
        return c;
      }
    }
    throw new IllegalArgumentException("unsupported currency: " + normalized);
  }
}
