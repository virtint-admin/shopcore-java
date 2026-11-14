package com.shopcore.tax;

import com.shopcore.catalog.CategoryPath;
import java.math.BigDecimal;

/**
 * A tax rate for a region and category.
 *
 * @param region {@code IN} (whole country) or {@code IN-KL} (one state)
 * @param category category path the rate applies to (with sub-categories), or {@code *} for all
 * @param ratePercent rate, e.g. 18 for 18%
 * @param pricesIncludeTax whether shelf prices in the region already contain the tax
 */
public record TaxRule(
    String region, String category, BigDecimal ratePercent, boolean pricesIncludeTax) {

  /** Validates the rate. */
  public TaxRule {
    if (ratePercent.signum() < 0) {
      throw new IllegalArgumentException("tax rate must not be negative");
    }
  }

  /**
   * Whether this rule covers {@code where} and {@code category}. A null category is matched only by
   * wildcard rules.
   */
  public boolean matches(Region where, String category) {
    Region mine = Region.parse(region);
    boolean regionOk =
        mine.subdivision().isEmpty() ? mine.sameCountry(where) : mine.code().equals(where.code());
    if (!regionOk) {
      return false;
    }
    if (this.category.equals("*")) {
      return true;
    }
    if (category == null) {
      return false;
    }
    CategoryPath ruleCat = CategoryPath.parse(this.category);
    CategoryPath itemCat = CategoryPath.parse(category);
    return ruleCat.toString().equalsIgnoreCase(itemCat.toString()) || ruleCat.isAncestorOf(itemCat);
  }

  /**
   * Ranking among matching rules: a deeper category wins first, then a state-level region over a
   * country-level one.
   */
  public int specificity() {
    int categoryDepth = category.equals("*") ? 0 : CategoryPath.parse(category).depth();
    int regionBonus = region.contains("-") ? 1 : 0;
    return categoryDepth * 10 - regionBonus;
  }
}
