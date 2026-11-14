// Issue 0034. Make this test pass by fixing the application code, not the test.
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

class Issue0034Test {

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
  void test_issue_0034() {
    assertAll(
        "issue 0034",
        () -> {
          assertEquals(List.of(), Catalog.standard().search("  "));
          assertEquals(List.of(), Catalog.standard().search(null));
        },
        () -> {
          assertEquals(
              List.of(
                  "Classic Cotton T-Shirt Black L",
                  "Classic Cotton T-Shirt Black M",
                  "Classic Cotton T-Shirt White M",
                  "Cotton Bedsheet Queen"),
              Catalog.standard().search("COTTON").stream().map(Product::name).toList());
        },
        () -> {
          assertEquals(List.of(), Catalog.standard().search("tea"));
        });
  }
}
