package com.shopcore.shipping;

import com.shopcore.catalog.Dimensions;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Chooses cartons for parcels. */
public final class BoxPicker {

  private BoxPicker() {}

  /** The warehouse's carton range, smallest first. */
  public static List<Box> standardBoxes() {
    return List.of(
        new Box("S", new Dimensions(250, 200, 100), 2000, 120),
        new Box("M", new Dimensions(350, 300, 150), 5000, 220),
        new Box("L", new Dimensions(450, 400, 300), 15000, 450),
        new Box("XL", new Dimensions(600, 500, 400), 25000, 800));
  }

  /** The carton with the smallest inner volume that fits the item, ties broken by code. */
  public static Optional<Box> smallestFitting(List<Box> boxes, Dimensions item, int grams) {
    return boxes.stream()
        .filter(b -> b.fits(item, grams))
        .min(Comparator.comparingLong((Box b) -> b.inner().volumeCm3()).thenComparing(Box::code));
  }
}
