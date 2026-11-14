package com.shopcore.inventory;

/**
 * When and how much to reorder for one SKU.
 *
 * @param reorderPoint reorder once available stock falls to this level
 * @param reorderQuantity minimum purchase-order size
 * @param safetyStock buffer the order should restore on top of the reorder point
 */
public record ReorderPolicy(int reorderPoint, int reorderQuantity, int safetyStock) {

  /** Rejects negative settings and an empty order size. */
  public ReorderPolicy {
    if (reorderPoint < 0 || safetyStock < 0) {
      throw new IllegalArgumentException("reorder point and safety stock must not be negative");
    }
    if (reorderQuantity <= 0) {
      throw new IllegalArgumentException("reorder quantity must be positive");
    }
  }

  /**
   * Classic reorder point: expected demand over the supplier lead time plus safety stock.
   * Fractional demand rounds up.
   */
  public static int reorderPointFor(double dailyDemand, int leadTimeDays, int safetyStock) {
    if (dailyDemand < 0 || leadTimeDays < 0 || safetyStock < 0) {
      throw new IllegalArgumentException("inputs must not be negative");
    }
    return (int) Math.ceil(dailyDemand * leadTimeDays) - safetyStock;
  }

  /** Stock is due for reordering when available units are at or below the reorder point. */
  public boolean needsReorder(int available) {
    return available <= reorderPoint;
  }

  /**
   * Units to order now: zero above the reorder point, otherwise enough to get back to reorder point
   * plus safety stock, but never less than the reorder quantity.
   */
  public int orderQuantity(int available) {
    if (!needsReorder(available)) {
      return 0;
    }
    int gap = reorderPoint + safetyStock - available;
    return Math.max(reorderQuantity, gap);
  }
}
