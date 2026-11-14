// Issue 0294. Make this test pass by fixing the application code, not the test.
package com.shopcore.shipping;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import org.junit.jupiter.api.Test;

class Issue0294Test {

  private static final RateTable STD = RateTable.standard();

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  @Test
  void test_issue_0294() {
    assertAll(
        "issue 0294",
        () -> {
          RateTable t = new RateTable();
          t.addSlab(Zone.LOCAL, 1000, inr("30"));
          t.setExtraPerKg(Zone.LOCAL, inr("10"));
          assertEquals(inr("40"), t.rateFor(Zone.LOCAL, 1500));
        },
        () -> {
          RateTable t = new RateTable();
          t.addSlab(Zone.LOCAL, 1000, inr("30"));
          t.setExtraPerKg(Zone.LOCAL, inr("10"));
          t.setExtraPerKg(Zone.LOCAL, inr("12"));
          assertEquals(inr("54"), t.rateFor(Zone.LOCAL, 3000));
        },
        () -> {
          assertEquals(inr("420"), STD.rateFor(Zone.REMOTE, 5001));
          assertEquals(inr("165"), STD.rateFor(Zone.LOCAL, 5500));
        },
        () -> {
          RateTable t = new RateTable();
          t.addSlab(Zone.LOCAL, 1000, inr("30"));
          assertThrows(IllegalArgumentException.class, () -> t.rateFor(Zone.LOCAL, 1001));
        });
  }
}
