// Issue 0028. Make this test pass by fixing the application code, not the test.
package com.shopcore.catalog;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Issue0028Test {

  private static List<String> skus(List<Product> products) {
    return products.stream().map(p -> p.sku().toString()).toList();
  }

  private static Product mug(String sku) {
    return new Product(
        Sku.parse(sku),
        "Mug",
        "Home > Kitchen",
        Money.of("349", Currency.INR),
        400,
        new Dimensions(120, 90, 100),
        true,
        Set.of());
  }

  @Test
  void test_issue_0028() {
    assertAll(
        "issue 0028",
        () -> {
          assertFalse(Catalog.standard().deactivate("GRO-100052"));
        },
        () -> {
          Catalog c = Catalog.standard();
          assertTrue(c.deactivate("ELE-555012"));
          assertEquals(14, c.activeCount());
          assertFalse(c.findBySku("ELE-555012").orElseThrow().active());
        },
        () -> {
          assertThrows(IllegalArgumentException.class, () -> Catalog.standard().deactivate("TOY-500012"));
        });
  }
}
