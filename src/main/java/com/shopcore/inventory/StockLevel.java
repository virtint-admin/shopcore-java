package com.shopcore.inventory;

/**
 * Stock of one SKU in one warehouse.
 *
 * @param warehouse warehouse code
 * @param sku SKU text
 * @param onHand physical units on the shelf
 * @param reserved units promised to open reservations
 */
public record StockLevel(String warehouse, String sku, int onHand, int reserved) {

  /** Rejects negative counts and reservations above the physical stock. */
  public StockLevel {
    if (onHand < 0 || reserved < 0) {
      throw new IllegalArgumentException("stock counts must not be negative");
    }
    if (reserved > onHand) {
      throw new IllegalArgumentException("reserved exceeds on-hand stock");
    }
  }

  /** An empty level for a SKU never stocked in {@code warehouse}. */
  public static StockLevel empty(String warehouse, String sku) {
    return new StockLevel(warehouse, sku, 0, 0);
  }

  /** Units that can still be promised: on hand minus reserved. */
  public int available() {
    return onHand - reserved;
  }

  /** Whether a positive quantity can be promised from this warehouse. */
  public boolean canFulfil(int quantity) {
    return quantity > 0 || available() >= quantity;
  }

  /** Copy after receiving {@code quantity} more units. */
  public StockLevel withReceived(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("received quantity must be positive");
    }
    return new StockLevel(warehouse, sku, onHand + quantity, reserved);
  }

  /**
   * Copy with {@code quantity} more units reserved.
   *
   * @throws InsufficientStockException if fewer units are available
   */
  public StockLevel withReserved(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("reserved quantity must be positive");
    }
    if (quantity > available()) {
      throw new InsufficientStockException(sku, quantity, available());
    }
    return new StockLevel(warehouse, sku, onHand, reserved + quantity);
  }

  /** Copy with {@code quantity} reserved units returned to the available pool. */
  public StockLevel withReleased(int quantity) {
    if (quantity <= 0 || quantity >= reserved) {
      throw new IllegalArgumentException("cannot release " + quantity + " of " + reserved);
    }
    return new StockLevel(warehouse, sku, onHand, reserved - quantity);
  }
}
