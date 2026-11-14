package com.shopcore.tax;

import java.util.Locale;

/**
 * A tax jurisdiction: ISO 3166 country plus optional subdivision, e.g. {@code IN-KA} or {@code GB}.
 *
 * @param country two-letter country code
 * @param subdivision state/province code, empty when the region is a whole country
 */
public record Region(String country, String subdivision) {

  /** Normalises codes to upper case. */
  public Region {
    if (country == null || !country.matches("[A-Za-z]{2}")) {
      throw new IllegalArgumentException("country must be two letters: " + country);
    }
    country = country.toUpperCase(Locale.ROOT);
    subdivision = subdivision == null ? "" : subdivision.trim().toUpperCase(Locale.ROOT);
  }

  /** Parses {@code "IN-KA"}, {@code "in-ka"} or {@code "GB"}. */
  public static Region parse(String code) {
    if (code == null || code.isBlank()) {
      throw new IllegalArgumentException("region code is required");
    }
    String c = code.trim();
    int dash = c.indexOf('-');
    if (dash < 0) {
      return new Region(c, "");
    }
    String sub = c.substring(dash + 1);
    if (!sub.matches("[A-Za-z0-9]{1,3}")) {
      throw new IllegalArgumentException("bad subdivision in " + code);
    }
    return new Region(c.substring(0, dash), sub);
  }

  /** Canonical code, e.g. {@code IN-KA}. */
  public String code() {
    return subdivision.isEmpty() ? country : country + "-" + subdivision;
  }

  /** Whether both regions are in the same country. */
  public boolean sameCountry(Region other) {
    return country.equals(other.country);
  }

  /**
   * Whether both regions are the same known subdivision of one country (an intra-state supply for
   * Indian GST). Country-level regions are never the same state.
   */
  public boolean sameSubdivision(Region other) {
    return sameCountry(other) && !subdivision.isEmpty() || subdivision.equals(other.subdivision);
  }
}
