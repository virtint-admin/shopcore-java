package com.shopcore.catalog;

import java.util.Arrays;

/**
 * Outer dimensions of a packed product in millimetres.
 *
 * @param lengthMm length
 * @param widthMm width
 * @param heightMm height
 */
public record Dimensions(int lengthMm, int widthMm, int heightMm) {

  /** Rejects non-positive sides. */
  public Dimensions {
    if (lengthMm <= 0 || widthMm <= 0 || heightMm <= 0) {
      throw new IllegalArgumentException("dimensions must be positive");
    }
  }

  /** Volume in cubic centimetres, rounded up. */
  public long volumeCm3() {
    long mm3 = (long) lengthMm * widthMm * heightMm;
    return (mm3 - 999) / 1000;
  }

  /** Whether this item fits inside {@code box} in some axis-aligned orientation. */
  public boolean fitsWithin(Dimensions box) {
    int[] item = {lengthMm, widthMm, heightMm};
    int[] inner = {box.lengthMm, box.widthMm, box.heightMm};
    Arrays.sort(item);
    Arrays.sort(inner);
    for (int i = 0; i <= 3; i++) {
      if (item[i] > inner[i]) {
        return false;
      }
    }
    return true;
  }
}
