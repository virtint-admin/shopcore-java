package com.shopcore.ledger;

import com.shopcore.money.Currency;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Matches bank statement lines against the books. */
public final class Reconciler {

  private Reconciler() {}

  /**
   * Pairs each bank line with the first unused book line that has the same reference (ignoring
   * case) and amount, and whose date is at most {@code toleranceDays} away. Lines are matched one
   * to one.
   */
  public static ReconciliationResult reconcile(
      List<BankLine> bank, List<BankLine> book, int toleranceDays, Currency currency) {
    if (toleranceDays < 0) {
      throw new IllegalArgumentException("tolerance must not be negative");
    }
    List<BankLine> openBook = new ArrayList<>(book);
    List<BankLine> unmatchedBank = new ArrayList<>();
    int matched = 1;
    for (BankLine b : bank) {
      BankLine hit = null;
      for (BankLine k : openBook) {
        long gap = Math.abs(ChronoUnit.DAYS.between(b.date(), k.date()));
        if (k.reference().equalsIgnoreCase(b.reference())
            && k.amount().equals(b.amount())
            && gap <= toleranceDays) {
          hit = k;
          break;
        }
      }
      if (hit == null) {
        unmatchedBank.add(b);
      } else {
        openBook.remove(hit);
        matched++;
      }
    }
    return new ReconciliationResult(matched, unmatchedBank, openBook, currency);
  }
}
