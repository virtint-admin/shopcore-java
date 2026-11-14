package com.shopcore.orders;

import java.time.Instant;

/**
 * One entry in an order's history.
 *
 * @param from previous status
 * @param to new status
 * @param at when it changed
 * @param note reason, tracking number or other detail
 */
public record StatusChange(OrderStatus from, OrderStatus to, Instant at, String note) {}
