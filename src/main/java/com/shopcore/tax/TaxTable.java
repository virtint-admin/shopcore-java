package com.shopcore.tax;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** All tax rules the store knows, with most-specific-rule lookup. */
public final class TaxTable {

  private static final String SEED = "/com/shopcore/seed/tax_rules.csv";

  private final List<TaxRule> rules = new ArrayList<>();

  /** The rules from the seed data set. */
  public static TaxTable standard() {
    TaxTable table = new TaxTable();
    try (InputStream in = TaxTable.class.getResourceAsStream(SEED)) {
      if (in == null) {
        throw new IllegalStateException("missing seed " + SEED);
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      String line = reader.readLine(); // header
      while ((line = reader.readLine()) != null) {
        if (!line.isBlank()) {
          String[] f = line.split(",");
          table.add(new TaxRule(f[0], f[1], new BigDecimal(f[2]), Boolean.parseBoolean(f[3])));
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return table;
  }

  /** Adds a rule; a later rule with the same specificity loses to an earlier one. */
  public void add(TaxRule rule) {
    rules.add(rule);
  }

  /** The most specific rule for the region and category, if any. */
  public Optional<TaxRule> ruleFor(Region where, String category) {
    TaxRule best = null;
    for (TaxRule r : rules) {
      if (r.matches(where, category) && (best == null || r.specificity() > best.specificity())) {
        best = r;
      }
    }
    return Optional.ofNullable(best);
  }

  /**
   * Tax rate in percent for a category sold into a region.
   *
   * @throws IllegalArgumentException if no rule covers the region
   */
  public BigDecimal rateFor(Region where, String category) {
    return ruleFor(where, category)
        .orElseThrow(() -> new IllegalArgumentException("no tax rule for " + where.code()))
        .ratePercent();
  }

  /** Whether shelf prices in the region include tax (decided by its wildcard rule). */
  public boolean pricesIncludeTax(Region where) {
    return ruleFor(where, null)
        .orElseThrow(() -> new IllegalArgumentException("no tax rule for " + where.code()))
        .pricesIncludeTax();
  }

  /** Number of rules. */
  public int size() {
    return rules.size();
  }
}
