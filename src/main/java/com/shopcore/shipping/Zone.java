package com.shopcore.shipping;

/** Courier zones, from cheapest and fastest to slowest. */
public enum Zone {
  /** Same sorting district. */
  LOCAL,
  /** Same postal region. */
  REGIONAL,
  /** Anywhere else on the mainland. */
  NATIONAL,
  /** North-East, Jammu &amp; Kashmir, island territories. */
  REMOTE;

  /** Business days in transit after dispatch. */
  public int transitDays() {
    return switch (this) {
      case LOCAL -> 1;
      case REGIONAL -> 2;
      case NATIONAL -> 4;
      case REMOTE -> 7;
    };
  }
}
