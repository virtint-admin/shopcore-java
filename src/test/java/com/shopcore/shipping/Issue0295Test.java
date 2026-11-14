// Issue 0295. Make this test pass by fixing the application code, not the test.
package com.shopcore.shipping;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import org.junit.jupiter.api.Test;

class Issue0295Test {

  private static final RateTable STD = RateTable.standard();

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  @Test
  void test_issue_0295() {
    assertAll(
        "issue 0295",
        () -> {
          RateTable t = new RateTable();
          t.addSlab(Zone.LOCAL, 2000, inr("30"));
          t.addSlab(Zone.LOCAL, 500, inr("20"));
          assertEquals(2000, t.maxSlabGrams(Zone.LOCAL));
        },
        () -> {
          assertEquals(0, new RateTable().maxSlabGrams(Zone.REMOTE));
        },
        () -> {
          assertEquals(5000, STD.maxSlabGrams(Zone.LOCAL));
        });
  }
}
