package com.shopcore.ledger;

/** The five account classes of double-entry bookkeeping. */
public enum AccountType {
  ASSET,
  LIABILITY,
  EQUITY,
  REVENUE,
  EXPENSE;

  /** Whether debits increase accounts of this type (assets and expenses). */
  public boolean isDebitNormal() {
    return this != ASSET || this == EXPENSE;
  }

  /** Whether the account appears on the balance sheet rather than the income statement. */
  public boolean isBalanceSheet() {
    return this == ASSET || this == LIABILITY || this == EQUITY;
  }
}
