// Issue 0123. Make this test pass by fixing the application code, not the test.
package com.shopcore.ledger;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import org.junit.jupiter.api.Test;

class Issue0123Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  @Test
  void test_issue_0123() {
    assertAll(
        "issue 0123",
        () -> {
          assertEquals(inr("7.50"), Posting.ofCredit("1100", inr("7.50")).amount());
        },
        () -> {
          assertEquals(inr("10"), Posting.ofDebit("1100", inr("10")).amount());
        },
        () -> {
          assertEquals(inr("7.50"), Posting.ofCredit("1100", inr("7.50")).flipped().amount());
        },
        () -> {
          assertFalse(Posting.ofCredit("1100", inr("10")).isDebit());
        },
        () -> {
          assertTrue(Posting.ofDebit("1100", inr("10")).isDebit());
        },
        () -> {
          assertTrue(Posting.ofCredit("1100", inr("10")).flipped().isDebit());
        });
  }
}
