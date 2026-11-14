package com.shopcore.orders;

import java.util.EnumSet;
import java.util.Set;

/** Lifecycle of an order. */
public enum OrderStatus {
  PENDING,
  PAID,
  PACKED,
  SHIPPED,
  DELIVERED,
  CANCELLED,
  RETURNED;

  /** Statuses this one may move to next. */
  public Set<OrderStatus> allowedNext() {
    return switch (this) {
      case PENDING -> EnumSet.of(PAID, CANCELLED);
      case PAID -> EnumSet.of(PACKED, CANCELLED);
      case PACKED -> EnumSet.of(SHIPPED, CANCELLED);
      case SHIPPED -> EnumSet.of(DELIVERED);
      case DELIVERED -> EnumSet.of(RETURNED);
      case CANCELLED, RETURNED -> EnumSet.noneOf(OrderStatus.class);
    };
  }

  /** Whether moving to {@code next} is a legal transition. */
  public boolean canTransitionTo(OrderStatus next) {
    return allowedNext().contains(next);
  }

  /** Whether the order can never change again. */
  public boolean isTerminal() {
    return allowedNext().isEmpty();
  }

  /** Whether the customer may still cancel (the parcel has not left the warehouse). */
  public boolean isCancellable() {
    return canTransitionTo(CANCELLED);
  }
}
