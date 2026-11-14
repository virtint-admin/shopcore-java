// Issue 0061. Make this test pass by fixing the application code, not the test.
package com.shopcore.catalog;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Issue0061Test {

  @Test
  void test_issue_0061() {
    assertAll(
        "issue 0061",
        () -> {
          assertEquals("mens-t-shirt-blue", Slugs.slugify("Men's T-Shirt (Blue)"));
        },
        () -> {
          assertEquals("", Slugs.slugify(null));
        },
        () -> {
          assertEquals("cafe-creme-500g", Slugs.slugify("Café Crème 500g"));
        },
        () -> {
          assertEquals("usb-c-charger", Slugs.slugify("  --USB-C   Charger!!  "));
        });
  }
}
