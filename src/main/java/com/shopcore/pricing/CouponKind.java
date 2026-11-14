package com.shopcore.pricing;

/** What a coupon takes off. */
public enum CouponKind {
  /** A percentage of the merchandise subtotal. */
  PERCENT,
  /** A fixed amount off the subtotal. */
  FIXED,
  /** Waives the shipping charge; no merchandise discount. */
  FREE_SHIPPING
}
