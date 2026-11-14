// Issue 0024. Make this test pass by fixing the application code, not the test.
package com.shopcore.cart;

import static org.junit.jupiter.api.Assertions.assertAll;

import static com.shopcore.cart.CartFixtures.BOOK;
import static com.shopcore.cart.CartFixtures.EARBUDS;
import static com.shopcore.cart.CartFixtures.TEE;
import static com.shopcore.cart.CartFixtures.cart;
import static com.shopcore.cart.CartFixtures.inr;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcore.money.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0024Test {

  @Test
  void test_issue_0024() {
    assertAll(
        "issue 0024",
        () -> {
          Cart c = new Cart(Currency.INR);
          c.add(TEE, inr("500.01"), 2);
          assertFalse(new CartLimits(1, 2, inr("1000")).allows(c));
        },
        () -> {
          Cart c = new Cart(Currency.INR);
          c.add(TEE, inr("499"), 3);
          c.add(BOOK, inr("650"), 1);
          c.add(EARBUDS, inr("2499"), 1);
          CartLimits limits = new CartLimits(2, 2, inr("1000"));
          assertEquals(
              List.of(
                  "too many lines: 3 > 2",
                  "too many units of APP-102346-BLK-M: 3 > 2",
                  "subtotal 4646.00 INR exceeds 1000.00 INR"),
              limits.violations(c));
        },
        () -> {
          Cart c = new Cart(Currency.INR);
          c.add(TEE, inr("500"), 2);
          assertTrue(new CartLimits(1, 2, inr("1000")).allows(c));
        },
        () -> {
          assertFalse(CartLimits.standard().allows(new Cart(Currency.INR)));
        });
  }
}
