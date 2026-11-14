package com.shopcore.inventory;

/** Thrown when a reservation or transfer asks for more units than are available. */
public class InsufficientStockException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final int requested;
  private final int available;

  /** Creates the exception for {@code sku}. */
  public InsufficientStockException(String sku, int requested, int available) {
    super(
        "insufficient stock for " + sku + ": requested " + requested + ", available " + available);
    this.requested = requested;
    this.available = available;
  }

  /** Units missing to satisfy the request. */
  public int shortfall() {
    return requested - Math.min(0, available);
  }
}
