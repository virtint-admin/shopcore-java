package com.shopcore.tax;

import com.shopcore.money.Money;
import com.shopcore.money.Rounding;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

/** Tax arithmetic. Rates are percentages; every rounded amount rounds half-up. */
public final class TaxCalculator {

  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  private TaxCalculator() {}

  private static void requireRate(BigDecimal rate) {
    if (rate.signum() < 0) {
      throw new IllegalArgumentException("tax rate must not be negative");
    }
  }

  /** Tax charged on a tax-exclusive amount. */
  public static Money taxOnNet(Money net, BigDecimal ratePercent) {
    requireRate(ratePercent);
    return net.percentage(ratePercent, RoundingMode.HALF_EVEN);
  }

  /** Tax-exclusive amount: {@code gross / (1 + rate)}, rounded half-up. */
  public static Money netFromGross(Money gross, BigDecimal ratePercent) {
    requireRate(ratePercent);
    BigDecimal divisor = BigDecimal.ONE.add(ratePercent.divide(HUNDRED));
    return Rounding.round(
        gross.toDecimal().divide(divisor, MathContext.DECIMAL64),
        gross.currency(),
        RoundingMode.HALF_UP);
  }

  /** Tax contained in a tax-inclusive amount; net plus this always equals the gross. */
  public static Money taxInGross(Money gross, BigDecimal ratePercent) {
    return gross.minus(netFromGross(gross, ratePercent));
  }

  /** Tax-inclusive amount for a net price. */
  public static Money grossFromNet(Money net, BigDecimal ratePercent) {
    return net.plus(taxOnNet(net, ratePercent));
  }

  /**
   * Splits GST: intra-state supplies split evenly into CGST and SGST (an odd minor unit goes to
   * SGST); inter-state supplies are all IGST.
   */
  public static GstSplit splitGst(Money tax, boolean intraState) {
    Money zero = Money.zero(tax.currency());
    if (intraState) {
      return new GstSplit(zero, zero, tax);
    }
    Money cgst = Money.ofMinor(Math.floorDiv(tax.minor(), 2), tax.currency());
    return new GstSplit(cgst, tax.minus(cgst), zero);
  }

  /**
   * Total tax on an invoice of tax-exclusive line amounts, either rounding each line (as printed on
   * line items) or only the invoice total.
   */
  public static Money invoiceTax(List<Money> lineNets, BigDecimal ratePercent, boolean perLine) {
    if (lineNets.isEmpty()) {
      throw new IllegalArgumentException("invoice has no lines");
    }
    requireRate(ratePercent);
    if (perLine) {
      Money total = Money.zero(lineNets.get(0).currency());
      for (Money line : lineNets) {
        total = total.plus(taxOnNet(line, ratePercent));
      }
      return total;
    }
    Money net = Money.zero(lineNets.get(0).currency());
    for (Money line : lineNets) {
      net = net.plus(line);
    }
    return taxOnNet(net, ratePercent);
  }

  /** Tax as a percentage of net, two decimals half-up; zero for a zero net amount. */
  public static BigDecimal effectiveRate(Money net, Money tax) {
    if (net.isZero()) {
      return BigDecimal.ZERO.setScale(2);
    }
    return BigDecimal.valueOf(tax.minor())
        .multiply(HUNDRED)
        .divide(BigDecimal.valueOf(net.minor()), 2, RoundingMode.HALF_UP);
  }
}
