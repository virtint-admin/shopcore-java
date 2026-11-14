package com.shopcore.pricing;

import com.shopcore.catalog.Product;
import com.shopcore.money.Money;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Scheduled markdowns on top of catalogue list prices. */
public final class PriceBook {

  private final List<Markdown> markdowns = new ArrayList<>();

  /**
   * Schedules a markdown.
   *
   * @throws IllegalArgumentException if it overlaps an existing markdown for the same SKU
   */
  public void addMarkdown(Markdown markdown) {
    for (Markdown m : markdowns) {
      if (m.overlaps(markdown)) {
        throw new IllegalArgumentException("overlapping markdown for " + markdown.sku());
      }
    }
    markdowns.add(markdown);
  }

  /**
   * Selling price at {@code now}: the active markdown's price when it is below list, otherwise the
   * list price.
   */
  public Money priceAt(Product product, Instant now) {
    String sku = product.sku().toString();
    for (Markdown m : markdowns) {
      if (m.sku().equals(sku)
          && m.isActiveAt(now)
          && product.price().isGreaterThan(m.salePrice())) {
        return m.salePrice();
      }
    }
    return product.price();
  }

  /** Markdowns running at {@code now}, sorted by SKU. */
  public List<Markdown> activeAt(Instant now) {
    return markdowns.stream()
        .filter(m -> m.isActiveAt(now))
        .sorted(Comparator.comparing(Markdown::sku))
        .toList();
  }

  /** Drops markdowns that have ended by {@code now}; returns how many were removed. */
  public int removeExpired(Instant now) {
    int before = markdowns.size();
    markdowns.removeIf(m -> now.isBefore(m.until()));
    return before - markdowns.size();
  }
}
