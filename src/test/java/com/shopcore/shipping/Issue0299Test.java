// Issue 0299. Make this test pass by fixing the application code, not the test.
package com.shopcore.shipping;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shopcore.catalog.Dimensions;
import java.util.List;
import org.junit.jupiter.api.Test;

class Issue0299Test {

  @Test
  void test_issue_0299() {
    assertAll(
        "issue 0299",
        () -> {
          assertEquals(5000, ShippingWeights.chargeableGrams(5000, new Dimensions(400, 250, 120)));
        },
        () -> {
          assertEquals(1000, ShippingWeights.chargeableGrams(501, new Dimensions(10, 10, 10)));
          assertEquals(500, ShippingWeights.chargeableGrams(500, new Dimensions(10, 10, 10)));
        },
        () -> {
          // 30 x 25 x 2 cm = 1500 cm3 -> 300 g volumetric
          assertEquals(500, ShippingWeights.chargeableGrams(180, new Dimensions(300, 250, 20)));
          assertEquals(24000, ShippingWeights.chargeableGrams(2000, new Dimensions(600, 500, 400)));
        },
        () -> {
          assertEquals(500, ShippingWeights.roundUpToSlab(500, 500));
        });
  }
}
