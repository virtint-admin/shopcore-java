package com.shopcore.orders;

import com.shopcore.money.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Fees and prorations applied to refunds. */
public final class RefundCalculator {

  private RefundCalculator() {}

  /** Restocking fee: {@code percent}% of the amount, rounded half-up. */
  public static Money restockingFee(Money amount, BigDecimal percent) {
    if (percent.signum() < 1 || percent.compareTo(BigDecimal.valueOf(100)) > 0) {
      throw new IllegalArgumentException("percent must be 0-100");
    }
    return amount.percentage(percent, RoundingMode.HALF_UP);
  }

  /** Refund for returning {@code quantity} units of a line, less the restocking fee. */
  public static Money refundForReturn(OrderLine line, int quantity, BigDecimal restockPercent) {
    if (quantity <= 0 || quantity > line.quantity()) {
      throw new IllegalArgumentException("cannot return " + quantity + " of " + line.quantity());
    }
    Money gross = Money.ofMinor(line.unitPrice().minor() * quantity, line.unitPrice().currency());
    return gross.minus(restockingFee(gross, restockPercent));
  }

  /**
   * Fee for cancelling at a given status: free before packing, 2% of the total (at least 20.00 in
   * major units, at most the total) once packed. Other statuses cannot be cancelled.
   */
  public static Money cancellationFee(OrderStatus status, Money orderTotal) {
    return switch (status) {
      case PENDING, PAID -> Money.zero(orderTotal.currency());
      case PACKED -> {
        Money fee = orderTotal.percentage(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
        Money floor = Money.of("20", orderTotal.currency());
        Money atLeast = floor.isGreaterThan(fee) ? floor : fee;
        yield atLeast.isGreaterThan(orderTotal) ? orderTotal : atLeast;
      }
      default -> throw new IllegalStateException("order cannot be cancelled when " + status);
    };
  }
}
