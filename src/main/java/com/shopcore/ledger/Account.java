package com.shopcore.ledger;

/**
 * A ledger account.
 *
 * @param code four-digit account code
 * @param name display name
 * @param type account class
 */
public record Account(String code, String name, AccountType type) {

  /** Validates the code. */
  public Account {
    if (code == null || !code.matches("\\d{4}")) {
      throw new IllegalArgumentException("account code must be 4 digits: " + code);
    }
  }
}
