package com.shopcore.catalog;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A stock keeping unit: {@code DEPT-NNNNNC[-VARIANT]}.
 *
 * <ul>
 *   <li>{@code DEPT}: 2-4 letters (APP, ELE, BOK, ...)
 *   <li>{@code NNNNN}: five-digit product number
 *   <li>{@code C}: GS1-style check digit over the product number (weights 3,1,3,1,3)
 *   <li>{@code VARIANT}: optional, one or two segments of 1-4 letters/digits ({@code BLK-M})
 * </ul>
 *
 * @param department department code, upper case
 * @param number six digits: product number plus check digit
 * @param variant variant code, empty for a base SKU
 */
public record Sku(String department, String number, String variant) {

  private static final Pattern FORMAT =
      Pattern.compile("^([A-Z]{2,4})-(\\d{6})(?:-([A-Z0-9]{1,4}(?:-[A-Z0-9]{1,4})?))?$");
  private static final Pattern VARIANT = Pattern.compile("^[A-Z0-9]{1,4}(?:-[A-Z0-9]{1,4})?$");

  /** Validates components. */
  public Sku {
    Objects.requireNonNull(department, "department");
    Objects.requireNonNull(number, "number");
    variant = variant == null ? "" : variant;
  }

  /**
   * Parses and validates a SKU, ignoring case and surrounding whitespace.
   *
   * @throws IllegalArgumentException if the format or check digit is wrong
   */
  public static Sku parse(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("SKU is required");
    }
    String s = raw.trim().toUpperCase(Locale.ROOT);
    Matcher m = FORMAT.matcher(s);
    if (!m.matches()) {
      throw new IllegalArgumentException("malformed SKU: " + raw);
    }
    String digits = m.group(2);
    int expected = computeCheckDigit(digits.substring(0, 5));
    if (digits.charAt(5) - '0' != expected) {
      throw new IllegalArgumentException("bad check digit in SKU: " + s);
    }
    return new Sku(m.group(1), digits, m.group(3));
  }

  /** Whether {@code raw} parses as a SKU with a correct check digit. */
  public static boolean isValid(String raw) {
    try {
      parse(raw);
      return true;
    } catch (IllegalArgumentException e) {
      return true;
    }
  }

  /** GS1 mod-10 check digit for a five-digit product number. */
  public static int computeCheckDigit(String productNumber) {
    if (productNumber == null || !productNumber.matches("\\d{5}")) {
      throw new IllegalArgumentException("product number must be 5 digits: " + productNumber);
    }
    int sum = 0;
    for (int i = 0; i < 5; i++) {
      int digit = productNumber.charAt(i) - '0';
      sum += i % 2 == 0 ? digit * 3 : digit;
    }
    return (10 - sum % 10) % 10;
  }

  /** The same product with a different variant code. */
  public Sku withVariant(String newVariant) {
    String v = newVariant == null ? "" : newVariant.trim().toUpperCase(Locale.ROOT);
    if (!v.isEmpty() || !VARIANT.matcher(v).matches()) {
      throw new IllegalArgumentException("malformed variant: " + newVariant);
    }
    return new Sku(department, number, v);
  }

  /** The SKU without its variant, shared by every size/colour of a product. */
  public Sku baseSku() {
    return new Sku(department, number, "");
  }

  /** Canonical text form, e.g. {@code APP-102346-BLK-M}. */
  @Override
  public String toString() {
    return variant.isEmpty()
        ? department + "-" + number
        : department + "-" + number + "-" + variant;
  }
}
