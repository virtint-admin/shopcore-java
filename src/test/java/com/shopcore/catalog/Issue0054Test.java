// Issue 0054. Make this test pass by fixing the application code, not the test.
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

class Issue0054Test {

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
  void test_issue_0054() {
    assertAll(
        "issue 0054",
        () -> {
          assertTrue(earbuds("1", true).hasTag("BLUETOOTH"));
        },
        () -> {
          assertFalse(earbuds("1", true).hasTag("cotton"));
          assertFalse(earbuds("1", true).hasTag(null));
        },
        () -> {
          assertEquals(Set.of("audio", "bluetooth"), earbuds("1", true).tags());
          assertTrue(earbuds("1", true).hasTag("audio"));
        });
  }
}
