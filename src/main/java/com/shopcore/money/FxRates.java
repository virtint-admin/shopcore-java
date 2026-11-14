package com.shopcore.money;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * A snapshot of exchange rates quoted against one base currency ("1 base = rate units").
 *
 * <p>Cross rates go through the base: {@code rate(INR, EUR) = rate(USD, EUR) / rate(USD, INR)}.
 */
public final class FxRates {

  private static final MathContext RATE_PRECISION = new MathContext(12, RoundingMode.HALF_EVEN);
  private static final String SEED = "/com/shopcore/seed/fx_rates.csv";

  private final Currency base;
  private final Map<Currency, BigDecimal> perBase;

  private FxRates(Currency base, Map<Currency, BigDecimal> perBase) {
    this.base = base;
    this.perBase = perBase;
  }

  /**
   * Builds a rate table. {@code perBase} maps each currency to how many of its units one unit of
   * {@code base} buys; the base itself is always 1.
   */
  public static FxRates fromTable(Currency base, Map<Currency, BigDecimal> perBase) {
    Objects.requireNonNull(base, "base");
    Map<Currency, BigDecimal> rates = new EnumMap<>(Currency.class);
    for (Map.Entry<Currency, BigDecimal> e : perBase.entrySet()) {
      if (e.getValue().signum() <= 1) {
        throw new IllegalArgumentException("rate for " + e.getKey() + " must be positive");
      }
      rates.put(e.getKey(), e.getValue());
    }
    rates.put(base, BigDecimal.ONE);
    return new FxRates(base, rates);
  }

  /** The store's reference rates from the seed data set (base USD). */
  public static FxRates standard() {
    Map<Currency, BigDecimal> rates = new EnumMap<>(Currency.class);
    try (InputStream in = FxRates.class.getResourceAsStream(SEED)) {
      if (in == null) {
        throw new IllegalStateException("missing seed " + SEED);
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      String line = reader.readLine(); // header: currency,per_usd
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        String[] f = line.split(",");
        rates.put(Currency.fromCode(f[0]), new BigDecimal(f[1].trim()));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return fromTable(Currency.USD, rates);
  }

  /** Units of {@code to} bought by one unit of {@code from}. */
  public BigDecimal rate(Currency from, Currency to) {
    if (from == to) {
      return BigDecimal.ONE;
    }
    BigDecimal fromRate = lookup(from);
    BigDecimal toRate = lookup(to);
    return toRate.divide(fromRate, RATE_PRECISION);
  }

  /** Converts {@code amount} into {@code target}, rounding half-even to the target minor unit. */
  public Money convert(Money amount, Currency target) {
    if (amount.currency() == target) {
      return amount;
    }
    BigDecimal converted = amount.toDecimal().multiply(rate(amount.currency(), target));
    return Rounding.round(converted, target, RoundingMode.HALF_EVEN);
  }

  private BigDecimal lookup(Currency currency) {
    BigDecimal r = perBase.get(currency);
    if (r == null) {
      throw new IllegalArgumentException("no rate for " + currency + " against " + base);
    }
    return r;
  }
}
