// Issue 0146. Make this test pass by fixing the application code, not the test.
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

class Issue0146Test {

  private static BankLine line(String ref, String amount) {
    return new BankLine(LocalDate.of(2026, 11, 2), ref, Money.of(amount, Currency.INR));
  }

  @Test
  void test_issue_0146() {
    assertAll(
        "issue 0146",
        () -> {
          ReconciliationResult r =
              new ReconciliationResult(
                  0, List.of(line("A", "100"), line("B", "-20")), List.of(line("C", "30")), Currency.INR);
          assertEquals(Money.of("50", Currency.INR), r.difference());
        },
        () -> {
          assertEquals(
              Money.zero(Currency.INR),
              new ReconciliationResult(1, List.of(), List.of(), Currency.INR).difference());
        },
        () -> {
          assertEquals(
              Money.of("-30", Currency.INR),
              new ReconciliationResult(0, List.of(), List.of(line("C", "30")), Currency.INR)
                  .difference());
        });
  }
}
