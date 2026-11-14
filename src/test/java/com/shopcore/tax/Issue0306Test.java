// Issue 0306. Make this test pass by fixing the application code, not the test.
package com.shopcore.tax;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Issue0306Test {

  @Test
  void test_issue_0306() {
    assertAll(
        "issue 0306",
        () -> {
          assertFalse(Region.parse("IN").sameSubdivision(Region.parse("IN")));
          assertFalse(Region.parse("US-KA").sameSubdivision(Region.parse("IN-KA")));
        },
        () -> {
          assertFalse(Region.parse("IN-KA").sameSubdivision(Region.parse("IN-MH")));
        },
        () -> {
          assertTrue(Region.parse("IN-KA").sameSubdivision(Region.parse("in-ka")));
        });
  }
}
