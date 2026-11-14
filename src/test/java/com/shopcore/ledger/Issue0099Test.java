// Issue 0099. Make this test pass by fixing the application code, not the test.
package com.shopcore.ledger;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Issue0099Test {

  @Test
  void test_issue_0099() {
    assertAll(
        "issue 0099",
        () -> {
          assertTrue(AccountType.ASSET.isDebitNormal());
          assertTrue(AccountType.EXPENSE.isDebitNormal());
        },
        () -> {
          assertFalse(AccountType.LIABILITY.isDebitNormal());
          assertFalse(AccountType.EQUITY.isDebitNormal());
        },
        () -> {
          assertFalse(AccountType.REVENUE.isDebitNormal());
        });
  }
}
