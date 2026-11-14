package com.shopcore.pricing;

import com.shopcore.money.Money;

/**
 * One step of a volume price list.
 *
 * @param minQuantity smallest quantity this unit price applies to
 * @param unitPrice price per unit from {@code minQuantity} upwards
 */
public record PriceTier(int minQuantity, Money unitPrice) {}
