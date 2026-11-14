// Issue 0233. Make this test pass by fixing the application code, not the test.
package com.shopcore.pricing;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.catalog.Catalog;
import com.shopcore.catalog.Product;
import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0233Test {

  private static final Instant D1 = Instant.parse("2026-11-01T00:00:00Z");
  private static final Instant D5 = Instant.parse("2026-11-05T00:00:00Z");
  private static final Instant D9 = Instant.parse("2026-11-09T00:00:00Z");
  private static final Product EARBUDS = Catalog.standard().findBySku("ELE-555012").orElseThrow();

  private static Money inr(String a) {
    return Money.of(a, Currency.INR);
  }

  private static Markdown md(String sku, String price, Instant from, Instant until) {
    return new Markdown(sku, inr(price), from, until);
  }

  @Test
  void test_issue_0233() {
    assertAll(
        "issue 0233",
        () -> {
          PriceBook book = new PriceBook();
          book.addMarkdown(md("ELE-555012", "1999", D5, D9));
          assertEquals(List.of(), book.activeAt(D1));
        },
        () -> {
          PriceBook book = new PriceBook();
          book.addMarkdown(md("ELE-555012", "1999", D1, D5));
          assertEquals(inr("2499"), book.priceAt(EARBUDS, D5));
        },
        () -> {
          PriceBook book = new PriceBook();
          book.addMarkdown(md("ELE-555012", "2999", D1, D5));
          assertEquals(inr("2499"), book.priceAt(EARBUDS, D1));
        },
        () -> {
          PriceBook book = new PriceBook();
          book.addMarkdown(md("ELE-555012", "1999", D1, D5));
          assertEquals(inr("1999"), book.priceAt(EARBUDS, D1));
        });
  }
}
