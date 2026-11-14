package com.shopcore.tax;

import com.shopcore.money.Money;
import java.math.BigDecimal;

/**
 * Net, tax and gross for an amount; {@code net + tax == gross} always holds.
 *
 * @param net tax-exclusive amount
 * @param tax tax amount
 * @param gross tax-inclusive amount
 */
public record TaxBreakdown(Money net, Money tax, Money gross) {

  /** Checks the invariant. */
  public TaxBreakdown {
    if (!net.plus(tax).equals(gross)) {
      throw new IllegalArgumentException("net + tax must equal gross");
    }
  }

  /** Breakdown for a tax-exclusive price. */
  public static TaxBreakdown fromNet(Money net, BigDecimal ratePercent) {
    Money tax = TaxCalculator.taxOnNet(net, ratePercent);
    return new TaxBreakdown(net, tax, net.plus(tax));
  }

  /** Breakdown for a tax-inclusive price. */
  public static TaxBreakdown fromGross(Money gross, BigDecimal ratePercent) {
    Money net = TaxCalculator.netFromGross(gross, ratePercent);
    return new TaxBreakdown(net, gross.minus(net), gross);
  }

  /** Component-wise sum. */
  public TaxBreakdown plus(TaxBreakdown other) {
    return new TaxBreakdown(net.plus(other.net), tax.plus(other.tax), gross.plus(other.gross));
  }
}
