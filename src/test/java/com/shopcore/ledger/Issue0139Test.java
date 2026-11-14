// Issue 0139. Make this test pass by fixing the application code, not the test.
package com.shopcore.ledger;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0139Test {

  private static BankLine line(int day, String ref, String amount) {
    return new BankLine(LocalDate.of(2026, 11, day), ref, Money.of(amount, Currency.INR));
  }

  @Test
  void test_issue_0139() {
    assertAll(
        "issue 0139",
        () -> {
          ReconciliationResult r =
              Reconciler.reconcile(
                  List.of(line(2, "A", "10"), line(2, "A", "10"), line(2, "B", "5")),
                  List.of(line(2, "A", "10"), line(2, "B", "5.01")),
                  0,
                  Currency.INR);
          assertEquals(1, r.matchedCount());
          assertEquals(List.of(line(2, "A", "10"), line(2, "B", "5")), r.unmatchedBank());
          assertEquals(List.of(line(2, "B", "5.01")), r.unmatchedBook());
          assertThrows(
              IllegalArgumentException.class,
              () -> Reconciler.reconcile(List.of(), List.of(), -1, Currency.INR));
        },
        () -> {
          ReconciliationResult r =
              Reconciler.reconcile(
                  List.of(line(3, "SETL-1", "1205.40")),
                  List.of(line(2, "setl-1", "1205.40")),
                  2,
                  Currency.INR);
          assertEquals(new ReconciliationResult(1, List.of(), List.of(), Currency.INR), r);
        },
        () -> {
          ReconciliationResult in =
              Reconciler.reconcile(
                  List.of(line(4, "A", "10")), List.of(line(2, "A", "10")), 2, Currency.INR);
          ReconciliationResult out =
              Reconciler.reconcile(
                  List.of(line(5, "A", "10")), List.of(line(2, "A", "10")), 2, Currency.INR);
          assertEquals(1, in.matchedCount());
          assertEquals(0, out.matchedCount());
        });
  }
}
