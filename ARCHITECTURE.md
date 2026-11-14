# Architecture

All code lives under `com.shopcore`. Packages depend only "downwards" in this list.

| Package | Responsibility | Main types |
|---|---|---|
| `money` | Fixed-point amounts, rounding, FX | `Money`, `Currency`, `Rounding`, `FxRates` |
| `catalog` | Products, SKUs with check digits, categories | `Catalog`, `Product`, `Sku`, `CategoryPath`, `Dimensions`, `Slugs` |
| `inventory` | Stock per warehouse, reservations, transfers, reordering | `Inventory`, `StockLevel`, `Reservation`, `ReorderPolicy`, `DemandForecast` |
| `pricing` | Discounts, coupons, volume tiers, bundles, markdowns, margins | `Discounts`, `Coupon`, `CouponCodes`, `TieredPricing`, `Bundle`, `PriceBook`, `Margins`, `PriceEndings` |
| `tax` | GST/VAT/sales-tax rules by region and category | `TaxTable`, `TaxRule`, `Region`, `TaxCalculator`, `TaxBreakdown`, `GstSplit` |
| `cart` | Basket, limits, checkout totals | `Cart`, `CartItem`, `CartLimits`, `CartPricer`, `CartTotals` |
| `orders` | Order state machine, cancellation, returns, refunds | `Order`, `OrderStatus`, `OrderIds`, `RefundCalculator` |
| `shipping` | Zones, rate slabs, chargeable weight, cartons, delivery dates | `PinCodes`, `RateTable`, `ShippingWeights`, `BoxPicker`, `DeliveryEstimator`, `ShippingCalculator` |
| `ledger` | Double-entry bookkeeping and bank reconciliation | `Ledger`, `JournalEntry`, `Posting`, `Postings`, `ChartOfAccounts`, `Reconciler` |
| `reports` | Sales aggregations, KPIs, CSV export | `SalesReport`, `Kpis`, `CsvExport` |

## Seed data

`src/main/resources/com/shopcore/seed/` holds the demo data used by tests and reports:
`products.csv` (catalogue, INR prices), `fx_rates.csv` (per USD), `tax_rules.csv` and
`shipping_rates.csv`. Each is loaded by a `standard()` factory (`Catalog.standard()`,
`FxRates.standard()`, `TaxTable.standard()`, `RateTable.standard()`).

## Key rules

- A checkout prices as: subtotal, minus coupon discount, then tax (extracted when prices include
  tax, added otherwise), plus shipping, which is never taxed.
- Orders move PENDING, PAID, PACKED, SHIPPED, DELIVERED; cancellation is allowed until shipped,
  returns for 30 days after delivery.
- Every ledger entry must balance; balances are reported in each account's normal direction.
- Report days are calendar days in the store's time zone (Asia/Kolkata), not UTC.
