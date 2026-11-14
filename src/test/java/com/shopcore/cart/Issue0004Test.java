// Issue 0004. Make this test pass by fixing the application code, not the test.
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

class Issue0004Test {

  private static final Catalog CATALOG = Catalog.standard();

  @Test
  void test_issue_0004() {
    assertAll(
        "issue 0004",
        () -> {
          Cart mine = cart();
          Cart guest = new Cart(Currency.INR);
          guest.add(TEE, inr("479"), 9);
          guest.add(EARBUDS, inr("2499"), 1);
          mine.merge(guest, 10);
          assertEquals(
              List.of(
                  new CartItem(TEE, inr("479"), 10),
                  new CartItem(BOOK, inr("650"), 1),
                  new CartItem(EARBUDS, inr("2499"), 1)),
              mine.lines());
        },
        () -> {
          Coupon mineCoupon = coupon(CouponKind.PERCENT, "10", "0");
          Coupon guestCoupon = coupon(CouponKind.FIXED, "50", "0");
          Cart guest = cart();
          guest.applyCoupon(guestCoupon, NOW);
          Cart withOwn = cart();
          withOwn.applyCoupon(mineCoupon, NOW);
          withOwn.merge(guest, 10);
          assertEquals(Optional.of(mineCoupon), withOwn.coupon());
          Cart without = new Cart(Currency.INR);
          without.merge(guest, 10);
          assertEquals(Optional.of(guestCoupon), without.coupon());
        },
        () -> {
          assertThrows(CurrencyMismatchException.class, () -> cart().merge(new Cart(Currency.USD), 10));
          assertThrows(IllegalArgumentException.class, () -> cart().merge(cart(), 0));
        });
  }
}
