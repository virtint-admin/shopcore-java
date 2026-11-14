// Issue 0199. Make this test pass by fixing the application code, not the test.
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

class Issue0199Test {

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
  void test_issue_0199() {
    assertAll(
        "issue 0199",
        () -> {
          assertFalse(PHONE_KIT.isComplete(Map.of()));
        },
        () -> {
          assertFalse(THREE_TEES.isComplete(Map.of(TEE, 2)));
        },
        () -> {
          assertTrue(THREE_TEES.isComplete(Map.of(TEE, 3)));
        });
  }
}
