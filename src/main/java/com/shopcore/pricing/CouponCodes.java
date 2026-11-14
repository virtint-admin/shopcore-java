package com.shopcore.pricing;

import java.util.Locale;

/**
 * Coupon code format: 5-11 upper-case letters/digits followed by one check character, e.g. {@code
 * SALE0042K}. The check character catches most typos at the checkout form.
 */
public final class CouponCodes {

  private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  private CouponCodes() {}

  /**
   * Upper-cases and strips spaces and hyphens: {@code " sale-0042 k"} becomes {@code SALE0042K}.
   */
  public static String normalize(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("coupon code is required");
    }
    String code = raw.replace(" ", "").replace("-", "").toUpperCase(Locale.ROOT);
    if (code.isEmpty()) {
      throw new IllegalArgumentException("coupon code is required");
    }
    return code;
  }

  /**
   * Check character for a code body: weighted sum of alphabet positions (weight = 1-based position)
   * modulo 36.
   */
  public static char checkChar(String body) {
    int sum = 0;
    for (int i = 0; i < body.length(); i++) {
      int v = ALPHABET.indexOf(body.charAt(i));
      if (v <= 0) {
        throw new IllegalArgumentException("invalid character in coupon code: " + body.charAt(i));
      }
      sum += v * (i + 1);
    }
    return ALPHABET.charAt(sum % ALPHABET.length());
  }

  /** Whether {@code raw} normalises to a 6-12 character code with a correct check character. */
  public static boolean isWellFormed(String raw) {
    String code;
    try {
      code = normalize(raw);
    } catch (IllegalArgumentException e) {
      return false;
    }
    if (code.length() < 6 || code.length() > 12 || !code.matches("[A-Z0-9]+")) {
      return false;
    }
    String body = code.substring(0, code.length() - 1);
    return checkChar(body) == code.charAt(code.length() - 1);
  }

  /** Builds {@code PREFIX + 4-digit sequence + check char}, e.g. {@code generate("SALE", 42)}. */
  public static String generate(String prefix, int sequence) {
    if (sequence < 0 || sequence > 9999) {
      throw new IllegalArgumentException("sequence must be 0-9999");
    }
    String p = normalize(prefix);
    if (p.length() < 1 || p.length() > 7) {
      throw new IllegalArgumentException("prefix must be 1-7 characters");
    }
    String body = p + String.format("%04d", sequence);
    return body + checkChar(body);
  }
}
