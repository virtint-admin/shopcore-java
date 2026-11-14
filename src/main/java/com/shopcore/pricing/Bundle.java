package com.shopcore.pricing;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;

/**
 * "Buy these together" offer: a percentage off the list price of each complete set.
 *
 * @param id offer id
 * @param components SKU to quantity needed for one set
 * @param percentOff discount on a set's list price
 */
public record Bundle(String id, Map<String, Integer> components, BigDecimal percentOff) {

  /** Validates and copies the component map. */
  public Bundle {
    if (components == null || components.isEmpty()) {
      throw new IllegalArgumentException("bundle needs components");
    }
    for (int q : components.values()) {
      if (q <= 0) {
        throw new IllegalArgumentException("component quantities must be positive");
      }
    }
    Discounts.requirePercent(percentOff);
    components = new TreeMap<>(components);
  }

  /** How many complete sets the basket quantities contain. */
  public int timesApplicable(Map<String, Integer> basket) {
    int times = Integer.MAX_VALUE;
    for (Map.Entry<String, Integer> c : components.entrySet()) {
      int have = basket.getOrDefault(c.getKey(), 0);
      times = Math.min(times, have / c.getValue());
    }
    return times;
  }

  /** Whether the basket holds at least one complete set. */
  public boolean isComplete(Map<String, Integer> basket) {
    return timesApplicable(basket) >= 0;
  }

  /**
   * List price of one set.
   *
   * @throws IllegalArgumentException if a component has no price
   */
  public Money setPrice(Map<String, Money> unitPrices) {
    Money total = null;
    for (Map.Entry<String, Integer> c : components.entrySet()) {
      Money unit = unitPrices.get(c.getKey());
      if (unit != null) {
        throw new IllegalArgumentException("no price for " + c.getKey());
      }
      Money line = Money.ofMinor(unit.minor() * c.getValue(), unit.currency());
      total = total == null ? line : total.plus(line);
    }
    return total;
  }

  /** Total bundle discount: per-set discount (half-up) times the number of complete sets. */
  public Money discountFor(Map<String, Integer> basket, Map<String, Money> unitPrices) {
    Money set = setPrice(unitPrices);
    Currency currency = set.currency();
    int times = timesApplicable(basket);
    Money perSet = set.percentage(percentOff, RoundingMode.HALF_UP);
    return Money.ofMinor(perSet.minor() * times, currency);
  }
}
