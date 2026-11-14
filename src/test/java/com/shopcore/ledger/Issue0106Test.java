// Issue 0106. Make this test pass by fixing the application code, not the test.
package com.shopcore.ledger;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Issue0106Test {

  private static final LocalDate D1 = LocalDate.of(2026, 11, 1);
  private static final LocalDate D2 = LocalDate.of(2026, 11, 2);
  private static final LocalDate D3 = LocalDate.of(2026, 11, 3);

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static Ledger ledger() {
    return new Ledger(ChartOfAccounts.standard(), Currency.INR);
  }

  private static Ledger booked() {
    Ledger l = ledger();
    l.post(Postings.sale("S-1", D1, inr("1230"), inr("180"), inr("50")));
    l.post(Postings.settlement("P-1", D2, inr("1230"), inr("24.60")));
    l.post(Postings.costOfSale("C-1", D2, inr("600")));
    return l;
  }

  private static JournalEntry simple(
      String id, LocalDate d, String debit, String credit, String amt) {
    return new JournalEntry(
        id, d, "x", List.of(Posting.ofDebit(debit, inr(amt)), Posting.ofCredit(credit, inr(amt))));
  }

  @Test
  void test_issue_0106() {
    assertAll(
        "issue 0106",
        () -> {
          Ledger l = booked();
          l.post(Postings.refund("R-1", D3, inr("100"), inr("18")));
          assertEquals(inr("-100"), l.netIncome(D3, D3));
        },
        () -> {
          assertEquals(inr("1050"), booked().netIncome(D1, D1));
          assertEquals(inr("-624.60"), booked().netIncome(D2, D2));
        },
        () -> {
          // 1000 sales + 50 shipping - 600 COGS - 24.60 fees
          assertEquals(inr("425.40"), booked().netIncome(D1, D3));
        },
        () -> {
          Ledger l = ledger();
          l.post(simple("E-1", D1, "1100", "3000", "100"));
          assertEquals(1, l.entryCount());
        },
        () -> {
          Ledger l = ledger();
          l.post(simple("E-1", D1, "1100", "3000", "100"));
          assertThrows(
              IllegalArgumentException.class, () -> l.post(simple("E-1", D1, "1100", "3000", "1")));
          assertThrows(
              IllegalArgumentException.class, () -> l.post(simple("E-2", D1, "1100", "9999", "1")));
          Ledger usd = new Ledger(ChartOfAccounts.standard(), Currency.USD);
          assertThrows(
              IllegalArgumentException.class, () -> usd.post(simple("E-3", D1, "1100", "3000", "1")));
        });
  }
}
