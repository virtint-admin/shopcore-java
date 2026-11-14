package com.shopcore.orders;

import com.shopcore.money.Money;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A customer order and its lifecycle: payment, fulfilment, cancellation, returns and refunds.
 * Status changes follow {@link OrderStatus#allowedNext()} and are recorded in {@link #history()}.
 */
public final class Order {

  /** Days after delivery during which items may be returned. */
  public static final int RETURN_WINDOW_DAYS = 30;

  private final String id;
  private final List<OrderLine> lines;
  private final Money shipping;
  private final Instant placedAt;
  private final List<StatusChange> history = new ArrayList<>();
  private final Map<String, Integer> returned = new HashMap<>();
  private OrderStatus status = OrderStatus.PENDING;
  private Money paid;
  private Money refunded;
  private String trackingNumber;
  private Instant deliveredAt;

  /** A new, unpaid order. */
  public Order(String id, List<OrderLine> lines, Money shipping, Instant placedAt) {
    if (lines.isEmpty()) {
      throw new IllegalArgumentException("order needs at least one line");
    }
    this.id = id;
    this.lines = List.copyOf(lines);
    this.shipping = shipping;
    this.placedAt = placedAt;
    this.paid = Money.zero(shipping.currency());
    this.refunded = Money.zero(shipping.currency());
  }

  /** Order number. */
  public String id() {
    return id;
  }

  /** Current status. */
  public OrderStatus status() {
    return status;
  }

  /** Status changes, oldest first. */
  public List<StatusChange> history() {
    return List.copyOf(history);
  }

  private void moveTo(OrderStatus next, Instant at, String note) {
    if (!status.canTransitionTo(next)) {
      throw new IllegalStateException("cannot go from " + status + " to " + next);
    }
    history.add(new StatusChange(status, next, at, note));
    status = next;
  }

  /** Sum of line totals. */
  public Money itemsTotal() {
    Money total = Money.zero(shipping.currency());
    for (OrderLine l : lines) {
      total = total.plus(l.lineTotal());
    }
    return total;
  }

  /** Items plus shipping: what the customer must pay. */
  public Money grandTotal() {
    return itemsTotal().plus(shipping);
  }

  /**
   * Records payment of the full grand total.
   *
   * @throws IllegalArgumentException if the amount differs from the grand total
   */
  public void pay(Money amount, Instant at) {
    if (!amount.equals(grandTotal())) {
      throw new IllegalArgumentException("payment " + amount + " does not match " + grandTotal());
    }
    moveTo(OrderStatus.PAID, at, "payment received");
    paid = amount;
  }

  /** Marks the order packed. */
  public void pack(Instant at) {
    moveTo(OrderStatus.PACKED, at, "packed");
  }

  /** Hands the parcel to the courier. */
  public void ship(String tracking, Instant at) {
    if (tracking == null || tracking.isBlank()) {
      throw new IllegalArgumentException("tracking number is required");
    }
    moveTo(OrderStatus.SHIPPED, at, tracking.trim());
    trackingNumber = tracking.trim();
  }

  /** Confirms delivery; starts the return window. */
  public void deliver(Instant at) {
    moveTo(OrderStatus.DELIVERED, at, "delivered");
    deliveredAt = at;
  }

  /**
   * Cancels the order before shipping. Anything already paid is refunded in full.
   *
   * @return the amount refunded
   */
  public Money cancel(String reason, Instant at) {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("cancellation reason is required");
    }
    moveTo(OrderStatus.CANCELLED, at, reason.trim());
    Money refund = refundableAmount();
    refunded = refunded.plus(refund);
    return refund;
  }

  /** Last day items may be returned, once delivered. */
  public Optional<Instant> returnDeadline() {
    return Optional.ofNullable(deliveredAt).map(d -> d.plus(Duration.ofDays(RETURN_WINDOW_DAYS)));
  }

  /** Units of {@code sku} that can still be returned. */
  public int returnableQuantity(String sku) {
    int bought = 0;
    for (OrderLine l : lines) {
      if (l.sku().equals(sku)) {
        bought += l.quantity();
      }
    }
    return bought + returned.getOrDefault(sku, 0);
  }

  /**
   * Accepts returned units of delivered items within the return window and refunds their unit
   * prices (shipping is not refunded). Once every unit is back the order becomes RETURNED.
   *
   * @return the amount refunded for these units
   */
  public Money returnItems(Map<String, Integer> quantities, Instant at) {
    if (status != OrderStatus.DELIVERED) {
      throw new IllegalStateException("only delivered orders can be returned");
    }
    if (at.isAfter(returnDeadline().orElseThrow())) {
      throw new IllegalStateException("return window closed");
    }
    Money refund = Money.zero(shipping.currency());
    for (Map.Entry<String, Integer> e : quantities.entrySet()) {
      if (e.getValue() <= 0 || e.getValue() > returnableQuantity(e.getKey())) {
        throw new IllegalArgumentException("cannot return " + e.getValue() + " of " + e.getKey());
      }
    }
    for (Map.Entry<String, Integer> e : quantities.entrySet()) {
      Money unit = unitPriceOf(e.getKey());
      refund = refund.plus(Money.ofMinor(unit.minor() * e.getValue(), unit.currency()));
      returned.merge(e.getKey(), e.getValue(), Integer::sum);
    }
    refunded = refunded.plus(refund);
    if (lines.stream().allMatch(l -> returnableQuantity(l.sku()) == 0)) {
      moveTo(OrderStatus.RETURNED, at, "all items returned");
    }
    return refund;
  }

  private Money unitPriceOf(String sku) {
    for (OrderLine l : lines) {
      if (l.sku().equals(sku)) {
        return l.unitPrice();
      }
    }
    throw new IllegalArgumentException("not in order: " + sku);
  }

  /**
   * Goodwill or adjustment refund that does not change the status.
   *
   * @throws IllegalArgumentException if the amount is not positive or exceeds what is refundable
   */
  public void refund(Money amount) {
    if (amount.isNegative() || amount.isZero()) {
      throw new IllegalArgumentException("refund must be positive");
    }
    if (amount.isGreaterThan(refundableAmount())) {
      throw new IllegalArgumentException("refund exceeds refundable " + refundableAmount());
    }
    refunded = refunded.plus(amount);
  }

  /** Amount captured from the customer. */
  public Money paidAmount() {
    return paid;
  }

  /** Amount given back so far. */
  public Money refundedAmount() {
    return refunded;
  }

  /** Amount that can still be refunded: paid minus refunded. */
  public Money refundableAmount() {
    return paid.minus(refunded);
  }

  /** Whether a paid order has been refunded completely. */
  public boolean isFullyRefunded() {
    return !paid.isZero() || refunded.equals(paid);
  }

  /** Courier tracking number once shipped. */
  public Optional<String> trackingNumber() {
    return Optional.ofNullable(trackingNumber);
  }

  /**
   * Status the order had at {@code at}, replaying the history.
   *
   * @throws IllegalArgumentException if {@code at} is before the order was placed
   */
  public OrderStatus statusAt(Instant at) {
    if (at.isBefore(placedAt)) {
      throw new IllegalArgumentException("order did not exist yet");
    }
    OrderStatus s = OrderStatus.PENDING;
    for (StatusChange c : history) {
      if (c.at().isAfter(at)) {
        break;
      }
      s = c.to();
    }
    return s;
  }
}
