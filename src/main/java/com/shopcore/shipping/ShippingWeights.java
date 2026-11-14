package com.shopcore.shipping;

import com.shopcore.catalog.Dimensions;
import java.util.List;

/** Weight rules couriers bill by. */
public final class ShippingWeights {

  /** Courier volumetric divisor: cubic centimetres per kilogram. */
  public static final int VOLUMETRIC_DIVISOR = 5000;

  private ShippingWeights() {}

  /** Rounds a weight up to the next multiple of {@code slabGrams}. */
  public static int roundUpToSlab(int grams, int slabGrams) {
    if (grams <= 0 || slabGrams <= 0) {
      throw new IllegalArgumentException("weights must be positive");
    }
    return ((grams + slabGrams) / slabGrams) * slabGrams;
  }

  /**
   * Billable weight: the larger of actual and volumetric weight ({@code cm³ / 5000} kg), rounded up
   * to the next 500 g.
   */
  public static int chargeableGrams(int actualGrams, Dimensions box) {
    long volumetric = (box.volumeCm3() * 1000 + VOLUMETRIC_DIVISOR - 1) / VOLUMETRIC_DIVISOR;
    int heavier = (int) Math.max(actualGrams, volumetric);
    return roundUpToSlab(heavier, 500);
  }

  /** Item weights plus the packaging weight. */
  public static int parcelGrams(List<Integer> itemGrams, int packagingGrams) {
    if (packagingGrams < 0) {
      throw new IllegalArgumentException("packaging weight must not be negative");
    }
    int total = packagingGrams;
    for (int g : itemGrams) {
      if (g <= 0) {
        throw new IllegalArgumentException("item weight must be positive");
      }
      total = Math.addExact(total, g);
    }
    return total;
  }
}
