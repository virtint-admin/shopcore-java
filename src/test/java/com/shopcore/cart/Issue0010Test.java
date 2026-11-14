// Issue 0010. Make this test pass by fixing the application code, not the test.
package com.shopcore.cart;

import static org.junit.jupiter.api.Assertions.assertAll;

import static com.shopcore.cart.CartFixtures.BOOK;
import static com.shopcore.cart.CartFixtures.EARBUDS;
import static com.shopcore.cart.CartFixtures.NOW;
import static com.shopcore.cart.CartFixtures.TEA;
import static com.shopcore.cart.CartFixtures.TEE;
import static com.shopcore.cart.CartFixtures.cart;
import static com.shopcore.cart.CartFixtures.coupon;
import static com.shopcore.cart.CartFixtures.inr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcore.catalog.Catalog;
import com.shopcore.money.Currency;
import com.shopcore.money.CurrencyMismatchException;
import com.shopcore.money.Money;
import com.shopcore.pricing.Coupon;
import com.shopcore.pricing.CouponKind;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class Issue0010Test {

  private static final Catalog CATALOG = Catalog.standard();

  @Test
  void test_issue_0010() {
    assertAll(
        "issue 0010",
        () -> {
          assertFalse(cart().removeCoupon());
        },
        () -> {
          Cart c = cart();
          c.applyCoupon(coupon(CouponKind.PERCENT, "10", "0"), NOW);
          assertTrue(c.removeCoupon());
          assertEquals(inr("0"), c.discount());
        },
        () -> {
          Cart c = cart();
          c.applyCoupon(coupon(CouponKind.PERCENT, "10", "0"), NOW);
          c.removeCoupon();
          assertFalse(c.removeCoupon());
        });
  }
}
