package com.shopcore.ledger;

import com.shopcore.money.Money;
import java.time.LocalDate;
import java.util.List;

/**
 * A dated set of postings that must balance.
 *
 * @param id entry id, unique in a ledger
 * @param date accounting date
 * @param memo description
 * @param postings at least two postings
 */
public record JournalEntry(String id, LocalDate date, String memo, List<Posting> postings) {

  /** Copies postings and requires at least two of them in one currency. */
  public JournalEntry {
    if (postings == null || postings.size() < 2) {
      throw new IllegalArgumentException("an entry needs at least two postings");
    }
    postings = List.copyOf(postings);
    for (Posting p : postings) {
      if (p.amount().currency() != postings.get(0).amount().currency()) {
        throw new IllegalArgumentException("an entry must use one currency");
      }
    }
  }

  /** Sum of debit sides. */
  public Money totalDebits() {
    Money total = Money.zero(postings.get(0).amount().currency());
    for (Posting p : postings) {
      total = total.plus(p.debit());
    }
    return total;
  }

  /** Sum of credit sides. */
  public Money totalCredits() {
    Money total = Money.zero(postings.get(0).amount().currency());
    for (Posting p : postings) {
      total = total.plus(p.credit());
    }
    return total;
  }

  /** Whether debits equal credits. */
  public boolean isBalanced() {
    return totalDebits().equals(totalCredits());
  }

  /** A mirror entry that cancels this one. */
  public JournalEntry reversal(String newId, LocalDate on) {
    return new JournalEntry(
        newId, on, "Reversal of " + id, postings.stream().map(Posting::flipped).toList());
  }
}
