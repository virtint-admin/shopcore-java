package com.shopcore.inventory;

import java.time.Instant;

/**
 * One change to on-hand stock, kept for audit.
 *
 * @param at when it happened
 * @param warehouse warehouse code
 * @param sku SKU text
 * @param delta change to on-hand units (negative for outbound)
 * @param type kind of movement
 * @param note free text: adjustment reason, transfer id or reservation id
 */
public record Movement(
    Instant at, String warehouse, String sku, int delta, MovementType type, String note) {}
