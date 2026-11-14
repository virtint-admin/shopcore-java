package com.shopcore.inventory;

import java.time.Instant;

/**
 * A completed stock transfer between warehouses.
 *
 * @param id transfer id, e.g. {@code TRF-0001}
 * @param from source warehouse
 * @param to destination warehouse
 * @param sku SKU text
 * @param quantity units moved
 * @param at when the transfer was booked
 */
public record Transfer(String id, String from, String to, String sku, int quantity, Instant at) {}
