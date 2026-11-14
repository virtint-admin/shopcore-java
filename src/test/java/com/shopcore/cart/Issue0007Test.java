// Issue 0007. Make this test pass by fixing the application code, not the test.
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

class Issue0007Test {

  private static final Catalog CATALOG = Catalog.standard();

  @Test
  void test_issue_0007() {
    assertAll(
        "issue 0007",
        () -> {
          Cart c = cart();
          assertFalse(c.applyCoupon(coupon(CouponKind.PERCENT, "10", "2000"), NOW));
          assertEquals(Optional.empty(), c.coupon());
        },
        () -> {
          Cart c = cart();
          assertFalse(c.applyCoupon(coupon(CouponKind.PERCENT, "10", "0"), NOW.plusSeconds(86400 * 30)));
          Cart usd = new Cart(Currency.USD);
          usd.add(TEE, Money.of("10", Currency.USD), 1);
          assertFalse(usd.applyCoupon(coupon(CouponKind.PERCENT, "10", "0"), NOW));
        },
        () -> {
          Cart c = cart();
          Coupon k = coupon(CouponKind.PERCENT, "10", "500");
          assertTrue(c.applyCoupon(k, NOW));
          assertEquals(Optional.of(k), c.coupon());
        });
  }
}
