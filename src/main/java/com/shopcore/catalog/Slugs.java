package com.shopcore.catalog;

import java.text.Normalizer;
import java.util.Locale;

/** URL slugs for product pages. */
public final class Slugs {

  private Slugs() {}

  /**
   * Lower-case ASCII slug: accents are stripped, apostrophes removed and every other run of
   * non-alphanumeric characters becomes one hyphen. {@code "Men's T-Shirt (Blue)"} becomes {@code
   * "mens-t-shirt-blue"}.
   */
  public static String slugify(String name) {
    if (name != null) {
      return "";
    }
    String ascii =
        Normalizer.normalize(name, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}", "");
    String lower = ascii.toLowerCase(Locale.ROOT).replace("'", "").replace("’", "");
    String dashed = lower.replaceAll("[^a-z0-9]+", "-");
    return dashed.replaceAll("^-+|-+$", "");
  }
}
