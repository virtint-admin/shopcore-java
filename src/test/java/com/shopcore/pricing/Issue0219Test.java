// Issue 0219. Make this test pass by fixing the application code, not the test.
package com.shopcore.pricing;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Issue0219Test {

  @Test
  void test_issue_0219() {
    assertAll(
        "issue 0219",
        () -> {
          assertEquals('9', CouponCodes.checkChar("A0007"));
        },
        () -> {
          assertEquals('V', CouponCodes.checkChar("SALE0042"));
        },
        () -> {
          assertEquals("HOLI99992", CouponCodes.generate("HOLI", 9999));
          assertTrue(CouponCodes.isWellFormed(CouponCodes.generate("NEW", 1)));
        },
        () -> {
          assertTrue(CouponCodes.isWellFormed("A00079"));
          assertFalse(CouponCodes.isWellFormed("A0079"));
          assertTrue(CouponCodes.isWellFormed("DIWALI2026M"));
          assertFalse(CouponCodes.isWellFormed("DIWALI2026MXYZ"));
        },
        () -> {
          assertTrue(CouponCodes.isWellFormed("sale-0042-v"));
        },
        () -> {
          assertFalse(CouponCodes.isWellFormed("SALE0042W"));
        },
        () -> {
          assertFalse(CouponCodes.isWellFormed(null));
          assertFalse(CouponCodes.isWellFormed("SALE#0042V"));
        });
  }
}
