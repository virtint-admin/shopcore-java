// Issue 0256. Make this test pass by fixing the application code, not the test.
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

class Issue0256Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  @Test
  void test_issue_0256() {
    assertAll(
        "issue 0256",
        () -> {
          assertEquals("\"a,b\"", CsvExport.escapeField("a,b"));
          assertEquals("\"say \"\"hi\"\"\"", CsvExport.escapeField("say \"hi\""));
          assertEquals("\"line\nbreak\"", CsvExport.escapeField("line\nbreak"));
          assertEquals("\"cr\rhere\"", CsvExport.escapeField("cr\rhere"));
        },
        () -> {
          assertEquals("a,\"b,c\",", CsvExport.row(Arrays.asList("a", "b,c", null)));
        },
        () -> {
          assertEquals("", CsvExport.row(List.of()));
        },
        () -> {
          assertEquals("x", CsvExport.row(List.of("x")));
        });
  }
}
