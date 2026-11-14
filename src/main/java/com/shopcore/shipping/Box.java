package com.shopcore.shipping;

import com.shopcore.catalog.Dimensions;

/**
 * A shipping carton.
 *
 * @param code carton code
 * @param inner inside dimensions
 * @param maxWeightGrams heaviest contents the carton is rated for
 * @param tareGrams weight of the empty carton
 */
public record Box(String code, Dimensions inner, int maxWeightGrams, int tareGrams) {

  /** Whether an item of these dimensions and contents weight fits and is within the rating. */
  public boolean fits(Dimensions item, int contentsGrams) {
    return item.fitsWithin(inner) && contentsGrams <= maxWeightGrams;
  }
}
