// Issue 0206. Make this test pass by fixing the application code, not the test.
package com.shopcore.pricing;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class Issue0206Test {

  private static final Instant FROM = Instant.parse("2026-10-01T00:00:00Z");
  private static final Instant UNTIL = Instant.parse("2026-11-01T00:00:00Z");
  private static final Instant MID = Instant.parse("2026-10-15T12:00:00Z");

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static Coupon coupon(CouponKind kind, String value, Money max, int maxUses, int used) {
    return new Coupon(
        "diwali-10", kind, new BigDecimal(value), inr("500"), max, FROM, UNTIL, maxUses, used);
  }

  private static Coupon percent10() {
    return coupon(CouponKind.PERCENT, "10", null, 0, 0);
  }

  @Test
  void test_issue_0206() {
    assertAll(
        "issue 0206",
        () -> {
          assertFalse(coupon(CouponKind.PERCENT, "10", null, 5, 5).hasUsesLeft());
        },
        () -> {
          assertFalse(percent10().isApplicable(inr("900"), UNTIL));
          assertFalse(coupon(CouponKind.PERCENT, "10", null, 1, 1).isApplicable(inr("900"), MID));
        },
        () -> {
          assertThrows(
              IllegalStateException.class, () -> coupon(CouponKind.PERCENT, "10", null, 2, 2).redeem());
        },
        () -> {
          assertEquals(3, coupon(CouponKind.PERCENT, "10", null, 5, 2).redeem().usedCount());
        },
        () -> {
          Coupon c = percent10().redeem();
          assertEquals("DIWALI10", c.code());
          assertEquals(1, c.usedCount());
        });
  }
}
