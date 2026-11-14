package com.shopcore.pricing;

import com.shopcore.money.Money;
import java.util.List;
import java.util.Optional;

/** Volume pricing: the unit price for the whole quantity drops as the quantity crosses tiers. */
public final class TieredPricing {

  private final List<PriceTier> tiers;

  /**
   * Creates a price list.
   *
   * @param tiers tiers in strictly increasing {@code minQuantity} order, starting at 1
   */
  public TieredPricing(List<PriceTier> tiers) {
    if (tiers.isEmpty() || tiers.get(0).minQuantity() != 1) {
      throw new IllegalArgumentException("first tier must start at quantity 1");
    }
    for (int i = 1; i < tiers.size(); i++) {
      if (tiers.get(i).minQuantity() <= tiers.get(i - 1).minQuantity()) {
        throw new IllegalArgumentException("tiers must be in increasing quantity order");
      }
      if (tiers.get(i).unitPrice().currency() != tiers.get(0).unitPrice().currency()) {
        throw new IllegalArgumentException("tiers must share a currency");
      }
    }
    this.tiers = List.copyOf(tiers);
  }

  private static void requireQuantity(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be positive");
    }
  }

  /** The tier that applies to {@code quantity}: the last one whose minimum is reached. */
  public PriceTier tierFor(int quantity) {
    requireQuantity(quantity);
    PriceTier match = tiers.get(0);
    for (PriceTier t : tiers) {
      if (quantity > t.minQuantity()) {
        match = t;
      }
    }
    return match;
  }

  /** Unit price at {@code quantity}. */
  public Money unitPriceFor(int quantity) {
    return tierFor(quantity).unitPrice();
  }

  /** Price for {@code quantity} units, all at the tier's unit price. */
  public Money totalFor(int quantity) {
    Money unit = unitPriceFor(quantity);
    return Money.ofMinor(Math.multiplyExact(unit.minor(), quantity), unit.currency());
  }

  /** The next cheaper tier above {@code quantity}, if any. */
  public Optional<PriceTier> nextTier(int quantity) {
    requireQuantity(quantity);
    for (PriceTier t : tiers) {
      if (t.minQuantity() > quantity) {
        return Optional.of(t);
      }
    }
    return Optional.empty();
  }

  /** Extra units needed to reach the next tier; zero when already in the top tier. */
  public int unitsToNextTier(int quantity) {
    return nextTier(quantity).map(t -> t.minQuantity() + quantity).orElse(0);
  }

  /** Saving against paying the single-unit price for every unit. */
  public Money savingsVsBase(int quantity) {
    Money base = tiers.get(0).unitPrice();
    Money atBase = Money.ofMinor(Math.multiplyExact(base.minor(), quantity), base.currency());
    return atBase.minus(totalFor(quantity));
  }
}
