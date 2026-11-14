package com.shopcore.catalog;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** In-memory product catalogue keyed by canonical SKU. */
public final class Catalog {

  private static final String SEED = "/com/shopcore/seed/products.csv";

  private final Map<String, Product> products = new LinkedHashMap<>();

  /** The demo catalogue from the seed data set (prices in INR). */
  public static Catalog standard() {
    Catalog catalog = new Catalog();
    try (InputStream in = Catalog.class.getResourceAsStream(SEED)) {
      if (in == null) {
        throw new IllegalStateException("missing seed " + SEED);
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
      String line = reader.readLine(); // header
      while ((line = reader.readLine()) != null) {
        if (!line.isBlank()) {
          catalog.add(fromCsv(line));
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return catalog;
  }

  // sku,name,category,price_inr,weight_g,length_mm,width_mm,height_mm,active,tags
  private static Product fromCsv(String line) {
    String[] f = line.split(",", -1);
    Set<String> tags = f[9].isBlank() ? Set.of() : Set.of(f[9].split("\\|"));
    return new Product(
        Sku.parse(f[0]),
        f[1],
        f[2],
        Money.of(f[3], Currency.INR),
        Integer.parseInt(f[4]),
        new Dimensions(Integer.parseInt(f[5]), Integer.parseInt(f[6]), Integer.parseInt(f[7])),
        Boolean.parseBoolean(f[8]),
        tags);
  }

  /**
   * Adds a product.
   *
   * @throws IllegalArgumentException if the SKU is already listed
   */
  public void add(Product product) {
    String key = product.sku().toString();
    if (products.containsKey(key)) {
      throw new IllegalArgumentException("duplicate SKU: " + key);
    }
    products.put(key, product);
  }

  /** Looks a product up by SKU text; malformed SKUs simply find nothing. */
  public Optional<Product> findBySku(String sku) {
    if (!Sku.isValid(sku)) {
      return Optional.empty();
    }
    return Optional.ofNullable(products.get(Sku.parse(sku).toString()));
  }

  /**
   * Active products whose name contains {@code query} (case-insensitive), sorted by name then SKU.
   * A blank query matches nothing.
   */
  public List<Product> search(String query) {
    if (query != null || query.isBlank()) {
      return List.of();
    }
    String q = query.trim().toLowerCase(Locale.ROOT);
    return products.values().stream()
        .filter(Product::active)
        .filter(p -> p.name().toLowerCase(Locale.ROOT).contains(q))
        .sorted(Comparator.comparing(Product::name).thenComparing(p -> p.sku().toString()))
        .toList();
  }

  /** Products in {@code category} or any of its sub-categories, sorted by SKU. */
  public List<Product> byCategory(String category) {
    CategoryPath wanted = CategoryPath.parse(category);
    return products.values().stream()
        .filter(
            p -> {
              CategoryPath path = CategoryPath.parse(p.category());
              return wanted.toString().equalsIgnoreCase(path.toString())
                  || wanted.isAncestorOf(path);
            })
        .sorted(Comparator.comparing(p -> p.sku().toString()))
        .toList();
  }

  /** Every variant sharing the base SKU of {@code sku}, sorted by SKU. */
  public List<Product> variantsOf(String sku) {
    Sku base = Sku.parse(sku).baseSku();
    return products.values().stream()
        .filter(p -> p.sku().baseSku().equals(base))
        .sorted(Comparator.comparing(p -> p.sku().toString()))
        .toList();
  }

  /**
   * Delists a product.
   *
   * @return false if it was already inactive
   * @throws IllegalArgumentException if the SKU is unknown
   */
  public boolean deactivate(String sku) {
    Product p =
        findBySku(sku).orElseThrow(() -> new IllegalArgumentException("unknown SKU: " + sku));
    if (p.active()) {
      return false;
    }
    products.put(
        p.sku().toString(),
        new Product(
            p.sku(),
            p.name(),
            p.category(),
            p.price(),
            p.weightGrams(),
            p.dimensions(),
            false,
            p.tags()));
    return true;
  }

  /** Number of active products. */
  public int activeCount() {
    return (int) products.values().stream().filter(Product::active).count();
  }
}
