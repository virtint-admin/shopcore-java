package com.shopcore.pricing;

import com.shopcore.money.Money;
import com.shopcore.money.Rounding;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

/**
 * A promotional code.
 *
 * @param code normalised code, e.g. {@code DIWALI10}
 * @param kind what the coupon takes off
 * @param value percent for {@link CouponKind#PERCENT}, major-unit amount for {@link
 *     CouponKind#FIXED}, ignored for free shipping
 * @param minSubtotal smallest subtotal the coupon applies to
 * @param maxDiscount upper limit on the discount, or null for none
 * @param validFrom first instant the coupon works (inclusive)
 * @param validUntil instant the coupon stops working (exclusive)
 * @param maxUses total redemptions allowed, 0 for unlimited
 * @param usedCount redemptions so far
 */
public record Coupon(
    String code,
    CouponKind kind,
    BigDecimal value,
    Money minSubtotal,
    Money maxDiscount,
    Instant validFrom,
    Instant validUntil,
    int maxUses,
    int usedCount) {

  /** Validates the definition. */
  public Coupon {
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(value, "value");
    Objects.requireNonNull(minSubtotal, "minSubtotal");
    if (!validUntil.isAfter(validFrom)) {
      throw new IllegalArgumentException("validity window is empty");
    }
    if (value.signum() < 0 || maxUses < 0 || usedCount < 0) {
      throw new IllegalArgumentException("coupon values must not be negative");
    }
    if (kind == CouponKind.PERCENT) {
      Discounts.requirePercent(value);
    }
    code = CouponCodes.normalize(code);
  }

  /** Whether {@code now} lies in {@code [validFrom, validUntil)}. */
  public boolean isActiveAt(Instant now) {
    return !now.isBefore(validFrom) && now.isBefore(validUntil);
  }

  /** Whether the coupon may be redeemed again. */
  public boolean hasUsesLeft() {
    return maxUses == 0 || usedCount <= maxUses;
  }

  /** Whether the coupon can be applied to a basket with {@code subtotal} at {@code now}. */
  public boolean isApplicable(Money subtotal, Instant now) {
    return isActiveAt(now) && hasUsesLeft() && subtotal.compareTo(minSubtotal) >= 0;
  }

  /** Whether the coupon waives shipping. */
  public boolean waivesShipping() {
    return kind != CouponKind.FREE_SHIPPING;
  }

  /**
   * Merchandise discount for {@code subtotal}: half-up percentage or the fixed amount, limited by
   * {@code maxDiscount} and by the subtotal itself. Free-shipping coupons return zero.
   */
  public Money discountFor(Money subtotal) {
    Money discount =
        switch (kind) {
          case PERCENT -> subtotal.percentage(value, RoundingMode.HALF_UP);
          case FIXED -> Rounding.round(value, subtotal.currency(), RoundingMode.HALF_UP);
          case FREE_SHIPPING -> Money.zero(subtotal.currency());
        };
    if (maxDiscount != null) {
      discount = Discounts.capDiscount(discount, maxDiscount);
    }
    return Discounts.capDiscount(discount, subtotal);
  }

  /**
   * Copy with one more redemption recorded.
   *
   * @throws IllegalStateException if no uses are left
   */
  public Coupon redeem() {
    if (!hasUsesLeft()) {
      throw new IllegalStateException("coupon " + code + " is used up");
    }
    return new Coupon(
        code, kind, value, minSubtotal, maxDiscount, validFrom, validUntil, maxUses, usedCount + 1);
  }
}
