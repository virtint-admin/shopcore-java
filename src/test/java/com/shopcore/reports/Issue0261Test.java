// Issue 0261. Make this test pass by fixing the application code, not the test.
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

class Issue0261Test {

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  @Test
  void test_issue_0261() {
    assertAll(
        "issue 0261",
        () -> {
          assertEquals(
              "rank,sku,units,revenue\r\n1,ELE-555012,1,2499.00\r\n2,BOK-000000,2,1300.00\r\n",
              CsvExport.topProductsCsv(
                  List.of(
                      new ProductSales("ELE-555012", 1, inr("2499")),
                      new ProductSales("BOK-000000", 2, inr("1300")))));
        },
        () -> {
          assertEquals(
              "rank,sku,units,revenue\r\n1,X,1,1500\r\n",
              CsvExport.topProductsCsv(
                  List.of(new ProductSales("X", 1, Money.of("1500", Currency.JPY)))));
        },
        () -> {
          assertEquals("rank,sku,units,revenue\r\n", CsvExport.topProductsCsv(List.of()));
        });
  }
}
