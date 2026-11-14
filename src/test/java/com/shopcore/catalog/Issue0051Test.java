// Issue 0051. Make this test pass by fixing the application code, not the test.
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

class Issue0051Test {

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
  void test_issue_0051() {
    assertAll(
        "issue 0051",
        () -> {
          assertFalse(earbuds("2499.00", false).isPurchasable());
        },
        () -> {
          assertFalse(earbuds("0", true).isPurchasable());
        },
        () -> {
          assertTrue(earbuds("2499.00", true).isPurchasable());
        });
  }
}
