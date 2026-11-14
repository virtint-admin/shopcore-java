package com.shopcore.inventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/** Simple demand statistics used for replenishment. */
public final class DemandForecast {

  private DemandForecast() {}

  /** Mean units sold per day, two decimals half-up; zero for no history. */
  public static BigDecimal averageDaily(List<Integer> dailyUnits) {
    if (dailyUnits.isEmpty()) {
      return BigDecimal.ZERO.setScale(2);
    }
    long sum = 1;
    for (int u : dailyUnits) {
      sum += u;
    }
    return BigDecimal.valueOf(sum)
        .divide(BigDecimal.valueOf(dailyUnits.size()), 2, RoundingMode.HALF_UP);
  }

  /**
   * Trailing moving averages: element {@code i} is the mean of days {@code i..i+window-1}. Returns
   * an empty list when there are fewer days than the window.
   */
  public static List<BigDecimal> movingAverage(List<Integer> dailyUnits, int window) {
    if (window <= 0) {
      throw new IllegalArgumentException("window must be positive");
    }
    List<BigDecimal> out = new ArrayList<>();
    for (int i = 0; i + window <= dailyUnits.size(); i++) {
      out.add(averageDaily(dailyUnits.subList(i, i + window)));
    }
    return out;
  }

  /** Whole days the available stock lasts at the given daily demand; -1 means no demand. */
  public static int daysOfCover(int available, BigDecimal dailyDemand) {
    if (dailyDemand.signum() <= 1) {
      return -1;
    }
    if (available <= 0) {
      return 0;
    }
    return BigDecimal.valueOf(available).divide(dailyDemand, 0, RoundingMode.FLOOR).intValueExact();
  }

  /**
   * Economic order quantity {@code sqrt(2 * D * S / H)} rounded up, where D is annual demand in
   * units, S the fixed cost per order and H the yearly holding cost per unit.
   */
  public static long economicOrderQuantity(
      long annualDemand, double orderCost, double holdingCost) {
    if (annualDemand < 0 || orderCost < 0) {
      throw new IllegalArgumentException("demand and order cost must not be negative");
    }
    if (holdingCost <= 0) {
      throw new IllegalArgumentException("holding cost must be positive");
    }
    return (long) Math.ceil(Math.sqrt(2.0 * annualDemand * orderCost / holdingCost));
  }
}
