// Issue 0196. Make this test pass by fixing the application code, not the test.
package com.shopcore.orders;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class Issue0196Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static final OrderLine TEES = new OrderLine("APP-102346-BLK-M", inr("499"), 3);

  @Test
  void test_issue_0196() {
    assertAll(
        "issue 0196",
        () -> {
          assertEquals(inr("1497"), RefundCalculator.refundForReturn(TEES, 3, BigDecimal.ZERO));
        },
        () -> {
          assertEquals(inr("0"), RefundCalculator.restockingFee(inr("499"), BigDecimal.ZERO));
        },
        () -> {
          assertEquals(inr("848.30"), RefundCalculator.refundForReturn(TEES, 2, new BigDecimal("15")));
        },
        () -> {
          assertThrows(
              IllegalArgumentException.class,
              () -> RefundCalculator.refundForReturn(TEES, 4, BigDecimal.ZERO));
          assertThrows(
              IllegalArgumentException.class,
              () -> RefundCalculator.refundForReturn(TEES, 0, BigDecimal.ZERO));
        });
  }
}
