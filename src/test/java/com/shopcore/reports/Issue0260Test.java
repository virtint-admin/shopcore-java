// Issue 0260. Make this test pass by fixing the application code, not the test.
package com.shopcore.reports;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class Issue0260Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  @Test
  void test_issue_0260() {
    assertAll(
        "issue 0260",
        () -> {
          TreeMap<LocalDate, Money> t = new TreeMap<>();
          t.put(LocalDate.of(2026, 11, 1), inr("1234567.89"));
          assertEquals("date,revenue\r\n2026-11-01,1234567.89\r\n", CsvExport.dailyTotalsCsv(t));
        },
        () -> {
          TreeMap<LocalDate, Money> t = new TreeMap<>();
          t.put(LocalDate.of(2026, 11, 2), inr("2998"));
          t.put(LocalDate.of(2026, 11, 1), inr("1648.5"));
          assertEquals(
              "date,revenue\r\n2026-11-01,1648.50\r\n2026-11-02,2998.00\r\n",
              CsvExport.dailyTotalsCsv(t));
        },
        () -> {
          assertEquals("date,revenue\r\n", CsvExport.dailyTotalsCsv(new TreeMap<>()));
        });
  }
}
