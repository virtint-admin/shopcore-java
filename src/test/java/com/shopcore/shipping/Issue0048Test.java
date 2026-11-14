// Issue 0048. Make this test pass by fixing the application code, not the test.
package com.shopcore.shipping;

import static org.junit.jupiter.api.Assertions.assertAll;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shopcore.catalog.Dimensions;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class Issue0048Test {

  private static final List<Box> STD = BoxPicker.standardBoxes();

  private static Optional<String> pick(Dimensions item, int grams) {
    return BoxPicker.smallestFitting(STD, item, grams).map(Box::code);
  }

  @Test
  void test_issue_0048() {
    assertAll(
        "issue 0048",
        () -> {
          assertEquals(Optional.of("M"), pick(new Dimensions(300, 250, 20), 180));
        },
        () -> {
          assertEquals(Optional.of("L"), pick(new Dimensions(120, 90, 100), 6000));
        },
        () -> {
          assertEquals(Optional.of("S"), pick(new Dimensions(120, 90, 100), 400));
        },
        () -> {
          List<Box> boxes =
              List.of(
                  new Box("B", new Dimensions(200, 200, 200), 1000, 10),
                  new Box("A", new Dimensions(200, 200, 200), 1000, 10));
          assertEquals(
              Optional.of("A"),
              BoxPicker.smallestFitting(boxes, new Dimensions(10, 10, 10), 1).map(Box::code));
        },
        () -> {
          assertEquals(Optional.empty(), pick(new Dimensions(700, 100, 100), 100));
        });
  }
}
