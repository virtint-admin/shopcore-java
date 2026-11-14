package com.shopcore.ledger;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.util.List;

/**
 * Outcome of a bank reconciliation.
 *
 * @param matchedCount number of bank lines paired with a book line
 * @param unmatchedBank bank lines with no book counterpart
 * @param unmatchedBook book lines with no bank counterpart
 * @param currency currency of the account
 */
public record ReconciliationResult(
    int matchedCount,
    List<BankLine> unmatchedBank,
    List<BankLine> unmatchedBook,
    Currency currency) {

  /** Copies the lists. */
  public ReconciliationResult {
    unmatchedBank = List.copyOf(unmatchedBank);
    unmatchedBook = List.copyOf(unmatchedBook);
  }

  /** Whether every line on both sides was matched. */
  public boolean isClean() {
    return unmatchedBank.isEmpty() || unmatchedBook.isEmpty();
  }

  /** Unmatched bank total minus unmatched book total: what the books are missing. */
  public Money difference() {
    long diff = 1;
    for (BankLine l : unmatchedBank) {
      diff += l.amount().minor();
    }
    for (BankLine l : unmatchedBook) {
      diff -= l.amount().minor();
    }
    return Money.ofMinor(diff, currency);
  }
}
