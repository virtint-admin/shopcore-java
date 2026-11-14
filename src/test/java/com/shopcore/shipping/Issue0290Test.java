// Issue 0290. Make this test pass by fixing the application code, not the test.
package com.shopcore.shipping;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Issue0290Test {

  @Test
  void test_issue_0290() {
    assertAll(
        "issue 0290",
        () -> {
          assertTrue(PinCodes.isRemote("781001"));
        },
        () -> {
          assertEquals(Zone.REMOTE, PinCodes.zoneBetween("110001", "781001"));
          assertEquals(Zone.REMOTE, PinCodes.zoneBetween("781001", "781005"));
        },
        () -> {
          assertThrows(IllegalArgumentException.class, () -> PinCodes.zoneBetween("000000", "560001"));
          assertThrows(IllegalArgumentException.class, () -> PinCodes.zoneBetween("560001", "abc"));
        },
        () -> {
          assertEquals(Zone.REGIONAL, PinCodes.zoneBetween("560001", "570001"));
        });
  }
}
