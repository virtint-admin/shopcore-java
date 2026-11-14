package com.shopcore.reports;

import com.shopcore.money.Money;

/**
 * Aggregated sales of one SKU.
 *
 * @param sku SKU text
 * @param units units sold
 * @param revenue net revenue
 */
public record ProductSales(String sku, int units, Money revenue) {}
