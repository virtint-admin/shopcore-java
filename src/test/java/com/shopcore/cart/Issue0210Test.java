// Issue 0210. Make this test pass by fixing the application code, not the test.
package com.shopcore.cart;

import static org.junit.jupiter.api.Assertions.assertAll;

import static com.shopcore.cart.CartFixtures.NOW;
import static com.shopcore.cart.CartFixtures.TEE;
import static com.shopcore.cart.CartFixtures.cart;
import static com.shopcore.cart.CartFixtures.coupon;
import static com.shopcore.cart.CartFixtures.inr;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shopcore.money.Currency;
import com.shopcore.pricing.CouponKind;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Issue0210Test {

  private static final BigDecimal GST = new BigDecimal("18");

  private static Cart discounted() {
    Cart c = cart();
    c.applyCoupon(coupon(CouponKind.PERCENT, "10", "500"), NOW);
    return c;
  }

  @Test
  void test_issue_0210() {
    assertAll(
        "issue 0210",
        () -> {
          assertEquals(
              new CartTotals(inr("1648"), inr("164.80"), inr("50"), inr("266.98"), inr("1800.18")),
              CartPricer.price(discounted(), GST, false, inr("50")));
        },
        () -> {
          Cart c = cart();
          c.applyCoupon(coupon(CouponKind.FREE_SHIPPING, "0", "0"), NOW);
          CartTotals t = CartPricer.price(c, GST, false, inr("50"));
          assertEquals(inr("0"), t.shipping());
          assertEquals(inr("1944.64"), t.total());
        },
        () -> {
          assertEquals(
              new CartTotals(inr("1648"), inr("164.80"), inr("50"), inr("226.25"), inr("1533.20")),
              CartPricer.price(discounted(), GST, true, inr("50")));
        },
        () -> {
          CartTotals t = CartPricer.price(new Cart(Currency.INR), GST, false, inr("50"));
          assertEquals(new CartTotals(inr("0"), inr("0"), inr("0"), inr("0"), inr("0")), t);
        });
  }
}
