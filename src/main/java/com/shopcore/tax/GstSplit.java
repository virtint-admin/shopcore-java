package com.shopcore.tax;

import com.shopcore.money.Money;

/**
 * Indian GST on one invoice, split by who collects it.
 *
 * @param cgst central GST (intra-state supplies)
 * @param sgst state GST (intra-state supplies)
 * @param igst integrated GST (inter-state supplies)
 */
public record GstSplit(Money cgst, Money sgst, Money igst) {

  /** The total tax across the three components. */
  public Money total() {
    return cgst.plus(sgst).plus(igst);
  }
}
