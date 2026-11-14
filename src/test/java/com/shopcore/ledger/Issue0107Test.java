// Issue 0107. Make this test pass by fixing the application code, not the test.
package com.shopcore.ledger;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class Issue0107Test {

  private static final ChartOfAccounts STD = ChartOfAccounts.standard();

  @Test
  void test_issue_0107() {
    assertAll(
        "issue 0107",
        () -> {
          assertEquals(
              List.of("4900", "5000", "5100", "5200"),
              STD.ofType(AccountType.EXPENSE).stream().map(Account::code).toList());
        },
        () -> {
          assertEquals(
              List.of("4000", "4100"),
              STD.ofType(AccountType.REVENUE).stream().map(Account::code).toList());
        },
        () -> {
          assertEquals(List.of(), new ChartOfAccounts().ofType(AccountType.EQUITY));
        });
  }
}
