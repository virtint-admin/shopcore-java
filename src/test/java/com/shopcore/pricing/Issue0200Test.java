// Issue 0200. Make this test pass by fixing the application code, not the test.
package com.shopcore.pricing;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Issue0200Test {

  private static final String PHONE = "ELE-500012";
  private static final String CHARGER = "ELE-500029";
  private static final String TEE = "APP-102346-BLK-M";

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static final Map<String, Money> PRICES =
      Map.of(PHONE, inr("18999"), CHARGER, inr("899"), TEE, inr("499"));
  private static final Bundle PHONE_KIT =
      new Bundle("KIT", Map.of(PHONE, 1, CHARGER, 1), new BigDecimal("10"));
  private static final Bundle THREE_TEES = new Bundle("TEES", Map.of(TEE, 3), new BigDecimal("15"));

  @Test
  void test_issue_0200() {
    assertAll(
        "issue 0200",
        () -> {
          assertEquals(inr("0"), PHONE_KIT.discountFor(Map.of(PHONE, 1), PRICES));
        },
        () -> {
          assertEquals(inr("1989.80"), PHONE_KIT.discountFor(Map.of(PHONE, 1, CHARGER, 3), PRICES));
        },
        () -> {
          Bundle b = new Bundle("C", Map.of(CHARGER, 1), new BigDecimal("12.5"));
          assertEquals(inr("224.76"), b.discountFor(Map.of(CHARGER, 2), PRICES));
        },
        () -> {
          assertEquals(inr("449.10"), THREE_TEES.discountFor(Map.of(TEE, 7), PRICES));
        },
        () -> {
          assertEquals(inr("1497"), THREE_TEES.setPrice(PRICES));
        },
        () -> {
          assertEquals(inr("19898"), PHONE_KIT.setPrice(PRICES));
        });
  }
}
