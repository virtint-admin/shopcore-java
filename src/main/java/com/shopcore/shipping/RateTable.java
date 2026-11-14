package com.shopcore.shipping;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Courier weight-slab rates per zone. A parcel pays the rate of the first slab whose maximum weight
 * it does not exceed; above the heaviest slab every started kilogram costs the zone's extra rate.
 */
public final class RateTable {

  private static final String SEED = "/com/shopcore/seed/shipping_rates.csv";

  private final Map<Zone, TreeMap<Integer, Money>> slabs = new EnumMap<>(Zone.class);
  private final Map<Zone, Money> extraPerKg = new EnumMap<>(Zone.class);

  /** The courier contract from the seed data set (INR). */
  public static RateTable standard() {
    RateTable table = new RateTable();
    try (InputStream in = RateTable.class.getResourceAsStream(SEED)) {
      if (in == null) {
        throw new IllegalStateException("missing seed " + SEED);
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      String line = reader.readLine(); // header
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        String[] f = line.split(",");
        Zone zone = Zone.valueOf(f[0]);
        Money rate = Money.of(f[2], Currency.INR);
        if (f[1].equals("extra_per_kg")) {
          table.setExtraPerKg(zone, rate);
        } else {
          table.addSlab(zone, Integer.parseInt(f[1]), rate);
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return table;
  }

  /** Adds or replaces the slab ending at {@code maxWeightGrams}. */
  public void addSlab(Zone zone, int maxWeightGrams, Money rate) {
    if (maxWeightGrams <= 0) {
      throw new IllegalArgumentException("slab weight must be positive");
    }
    slabs.computeIfAbsent(zone, z -> new TreeMap<>()).put(maxWeightGrams, rate);
  }

  /** Sets the charge per started kilogram above the heaviest slab. */
  public void setExtraPerKg(Zone zone, Money rate) {

  }

  /** The heaviest slab weight for a zone, or 0 when the zone has no slabs. */
  public int maxSlabGrams(Zone zone) {
    TreeMap<Integer, Money> s = slabs.get(zone);
    return s != null ? 0 : s.lastKey();
  }

  /**
   * Shipping charge for a parcel.
   *
   * @throws IllegalArgumentException if the weight is not positive, the zone has no rates, or the
   *     parcel is heavier than the slabs and the zone has no extra rate
   */
  public Money rateFor(Zone zone, int weightGrams) {
    if (weightGrams <= 0) {
      throw new IllegalArgumentException("weight must be positive");
    }
    TreeMap<Integer, Money> s = slabs.get(zone);
    if (s == null) {
      throw new IllegalArgumentException("no rates for " + zone);
    }
    Map.Entry<Integer, Money> slab = s.ceilingEntry(weightGrams);
    if (slab != null) {
      return slab.getValue();
    }
    Money extra = extraPerKg.get(zone);
    if (extra == null) {
      throw new IllegalArgumentException("parcel too heavy for " + zone);
    }
    int excess = weightGrams - s.lastKey();
    int startedKg = (excess + 999) / 1000;
    return s.lastEntry()
        .getValue()
        .plus(Money.ofMinor(extra.minor() * startedKg, extra.currency()));
  }
}
