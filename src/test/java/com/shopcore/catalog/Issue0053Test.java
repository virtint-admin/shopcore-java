// Issue 0053. Make this test pass by fixing the application code, not the test.
package com.shopcore.catalog;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Issue0053Test {

  private static Product earbuds(String price, boolean active) {
    return new Product(
        Sku.parse("ELE-555012"),
        "Wireless Earbuds",
        "Electronics > Audio",
        Money.of(price, Currency.INR),
        120,
        new Dimensions(100, 80, 40),
        active,
        Set.of("Bluetooth", " audio "));
  }

  @Test
  void test_issue_0053() {
    assertAll(
        "issue 0053",
        () -> {
          Product p = earbuds("2499.00", true).withPrice(Money.of("1999.00", Currency.INR));
          assertEquals(Money.of("1999.00", Currency.INR), p.price());
          assertEquals("Wireless Earbuds", p.name());
        },
        () -> {
          assertThrows(
              IllegalArgumentException.class,
              () -> earbuds("1", true).withPrice(Money.of("1", Currency.USD)));
        },
        () -> {
          assertThrows(
              IllegalArgumentException.class,
              () -> earbuds("1", true).withPrice(Money.of("-1", Currency.INR)));
        });
  }
}
