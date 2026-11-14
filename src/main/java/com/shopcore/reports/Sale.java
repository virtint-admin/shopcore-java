package com.shopcore.reports;

import com.shopcore.money.Money;
import java.time.Instant;

/**
 * One sold order line, as exported from the order system.
 *
 * @param orderId order number
 * @param sku SKU text
 * @param quantity units
 * @param net revenue for the line, excluding tax
 * @param at when the order was paid
 */
public record Sale(String orderId, String sku, int quantity, Money net, Instant at) {}
