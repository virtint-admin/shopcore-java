package com.shopcore.cart;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import com.shopcore.pricing.Coupon;
import com.shopcore.pricing.CouponKind;
import java.math.BigDecimal;
import java.time.Instant;

final class CartFixtures {

  static final String TEE = "APP-102346-BLK-M";
  static final String EARBUDS = "ELE-555012";
  static final String BOOK = "BOK-000000";
  static final String TEA = "GRO-100052";
  static final Instant NOW = Instant.parse("2026-10-15T12:00:00Z");

  private CartFixtures() {}

  static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  static Coupon coupon(CouponKind kind, String value, String min) {
    return new Coupon(
        "TEST",
        kind,
        new BigDecimal(value),
        inr(min),
        null,
        Instant.parse("2026-10-01T00:00:00Z"),
        Instant.parse("2026-11-01T00:00:00Z"),
        0,
        0);
  }

  static Cart cart() {
    Cart c = new Cart(Currency.INR);
    c.add(TEE, inr("499"), 2);
    c.add(BOOK, inr("650"), 1);
    return c;
  }
}
