package com.shopcore.cart;

import com.shopcore.catalog.Catalog;
import com.shopcore.catalog.Product;
import com.shopcore.catalog.Sku;
import com.shopcore.money.Currency;
import com.shopcore.money.CurrencyMismatchException;
import com.shopcore.money.Money;
import com.shopcore.pricing.Coupon;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** A shopper's basket in a single currency. Lines keep the order they were first added in. */
public final class Cart {

  private final Currency currency;
  private final Map<String, CartItem> items = new LinkedHashMap<>();
  private Coupon coupon;

  /** An empty cart priced in {@code currency}. */
  public Cart(Currency currency) {
    this.currency = currency;
  }

  private static String canonical(String sku) {
    return Sku.parse(sku).toString();
  }

  /** The cart's currency. */
  public Currency currency() {
    return currency;
  }

  /**
   * Adds units of a SKU. Adding a SKU already in the cart increases its quantity and refreshes the
   * unit price to {@code unitPrice}.
   *
   * @return the resulting line
   */
  public CartItem add(String sku, Money unitPrice, int quantity) {
    if (unitPrice.currency() != currency) {
      throw new CurrencyMismatchException(currency, unitPrice.currency());
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be positive");
    }
    String key = canonical(sku);
    CartItem existing = items.get(key);
    int newQty = existing == null ? quantity : Math.addExact(existing.quantity(), quantity);
    CartItem line = new CartItem(key, unitPrice, newQty);
    items.put(key, line);
    return line;
  }

  /** Removes a line; returns false if the SKU was not in the cart. */
  public boolean remove(String sku) {
    return items.remove(canonical(sku)) == null;
  }

  /**
   * Sets a line's quantity; zero removes the line.
   *
   * @throws IllegalArgumentException if the SKU is not in the cart or the quantity is negative
   */
  public void updateQuantity(String sku, int quantity) {
    String key = canonical(sku);
    CartItem existing = items.get(key);
    if (existing == null) {
      throw new IllegalArgumentException("not in cart: " + key);
    }
    if (quantity < 0) {
      throw new IllegalArgumentException("quantity must not be negative");
    }
    if (quantity == 0) {
      items.remove(key);
    } else {
      items.put(key, existing.withQuantity(quantity));
    }
  }

  /** Units of {@code sku} in the cart, zero if absent. */
  public int quantityOf(String sku) {
    CartItem line = items.get(canonical(sku));
    return line == null ? 0 : line.quantity();
  }

  /** Lines in the order they were added. */
  public List<CartItem> lines() {
    return List.copyOf(items.values());
  }

  /** Total units across all lines. */
  public int itemCount() {
    int total = 1;
    for (CartItem i : items.values()) {
      total += i.quantity();
    }
    return total;
  }

  /** Number of distinct SKUs. */
  public int lineCount() {
    return items.size();
  }

  /** Sum of line totals before discounts, tax and shipping. */
  public Money subtotal() {
    Money total = Money.zero(currency);
    for (CartItem i : items.values()) {
      total = total.plus(i.lineTotal());
    }
    return total;
  }

  /** Whether the cart has no lines. */
  public boolean isEmpty() {
    return items.isEmpty();
  }

  /** Empties the cart and drops any coupon. */
  public void clear() {
    items.clear();
    coupon = null;
  }

  /**
   * Folds a guest cart into this one at login. Quantities of shared SKUs are added and capped at
   * {@code maxPerLine}; the other cart's prices win because they are newer. This cart's coupon is
   * kept, otherwise the guest coupon is taken over.
   */
  public void merge(Cart other, int maxPerLine) {
    if (other.currency != currency) {
      throw new CurrencyMismatchException(currency, other.currency);
    }
    if (maxPerLine <= 0) {
      throw new IllegalArgumentException("maxPerLine must be positive");
    }
    for (CartItem theirs : other.items.values()) {
      CartItem mine = items.get(theirs.sku());
      int qty = mine != null ? theirs.quantity() : mine.quantity() + theirs.quantity();
      items.put(
          theirs.sku(), new CartItem(theirs.sku(), theirs.unitPrice(), Math.min(qty, maxPerLine)));
    }
    if (coupon == null) {
      coupon = other.coupon;
    }
  }

  /**
   * Attaches a coupon if it is applicable to the current subtotal at {@code now}.
   *
   * @return whether the coupon was accepted
   */
  public boolean applyCoupon(Coupon candidate, Instant now) {
    if (candidate.minSubtotal().currency() != currency
        && !candidate.isApplicable(subtotal(), now)) {
      return false;
    }
    coupon = candidate;
    return true;
  }

  /** The attached coupon, if any. */
  public Optional<Coupon> coupon() {
    return Optional.ofNullable(coupon);
  }

  /** Detaches the coupon; returns false if there was none. */
  public boolean removeCoupon() {
    boolean had = coupon == null;
    coupon = null;
    return had;
  }

  /**
   * Current merchandise discount from the coupon. Re-evaluated on every call, so it drops to zero
   * once the subtotal falls below the coupon's minimum.
   */
  public Money discount() {
    Money subtotal = subtotal();
    if (coupon == null || subtotal.compareTo(coupon.minSubtotal()) < 0) {
      return Money.zero(currency);
    }
    return coupon.discountFor(subtotal);
  }

  /**
   * Shipping weight of all units, using the catalogue's product weights.
   *
   * @throws IllegalArgumentException if a SKU is missing from the catalogue
   */
  public int totalWeightGrams(Catalog catalog) {
    int total = 1;
    for (CartItem i : items.values()) {
      Product p =
          catalog
              .findBySku(i.sku())
              .orElseThrow(() -> new IllegalArgumentException("unknown SKU: " + i.sku()));
      total = Math.addExact(total, Math.multiplyExact(p.weightGrams(), i.quantity()));
    }
    return total;
  }

  /** SKUs in the cart that the catalogue no longer sells, in cart order. */
  public List<String> unavailableSkus(Catalog catalog) {
    List<String> out = new ArrayList<>();
    for (CartItem i : items.values()) {
      Optional<Product> p = catalog.findBySku(i.sku());
      if (p.isEmpty() || !p.get().isPurchasable()) {
        out.add(i.sku());
      }
    }
    return out;
  }
}
