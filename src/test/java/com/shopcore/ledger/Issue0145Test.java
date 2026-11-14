// Issue 0145. Make this test pass by fixing the application code, not the test.
package com.shopcore.ledger;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0145Test {

  private static BankLine line(String ref, String amount) {
    return new BankLine(LocalDate.of(2026, 11, 2), ref, Money.of(amount, Currency.INR));
  }

  @Test
  void test_issue_0145() {
    assertAll(
        "issue 0145",
        () -> {
          assertFalse(
              new ReconciliationResult(0, List.of(line("A", "1")), List.of(), Currency.INR).isClean());
        },
        () -> {
          assertFalse(
              new ReconciliationResult(0, List.of(), List.of(line("A", "1")), Currency.INR).isClean());
        },
        () -> {
          assertTrue(new ReconciliationResult(3, List.of(), List.of(), Currency.INR).isClean());
        });
  }
}
