package com.shopcore.ledger;

import com.shopcore.money.Money;

/**
 * One side of a journal entry: a debit or a credit to one account.
 *
 * @param accountCode account
 * @param debit debit amount (zero for a credit)
 * @param credit credit amount (zero for a debit)
 */
public record Posting(String accountCode, Money debit, Money credit) {

  /** Exactly one side must be positive and the other zero. */
  public Posting {
    if (debit.currency() != credit.currency()) {
      throw new IllegalArgumentException("debit and credit currencies differ");
    }
    if (debit.isNegative() || credit.isNegative()) {
      throw new IllegalArgumentException("posting amounts must not be negative");
    }
    if (debit.isZero() == credit.isZero()) {
      throw new IllegalArgumentException("posting must be either a debit or a credit");
    }
  }

  /** A debit of {@code amount} to {@code accountCode}. */
  public static Posting ofDebit(String accountCode, Money amount) {
    return new Posting(accountCode, amount, Money.zero(amount.currency()));
  }

  /** A credit of {@code amount} to {@code accountCode}. */
  public static Posting ofCredit(String accountCode, Money amount) {
    return new Posting(accountCode, Money.zero(amount.currency()), amount);
  }

  /** Whether this posting is a debit. */
  public boolean isDebit() {
    return debit.isZero();
  }

  /** The posting's amount, whichever side it is on. */
  public Money amount() {
    return isDebit() ? debit : credit;
  }

  /** The same amount on the opposite side, used for reversals. */
  public Posting flipped() {
    return new Posting(accountCode, credit, debit);
  }
}
