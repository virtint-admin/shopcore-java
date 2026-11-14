package com.shopcore.inventory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Stock across all warehouses: receipts, adjustments, checkout reservations, commits and
 * warehouse-to-warehouse transfers. Every change to on-hand stock is logged as a {@link Movement}.
 */
public final class Inventory {

  private final Map<String, StockLevel> levels = new TreeMap<>();
  private final Map<String, Reservation> reservations = new LinkedHashMap<>();
  private final List<Movement> movements = new ArrayList<>();
  private int reservationSeq;
  private int transferSeq;

  private static String key(String warehouse, String sku) {
    return warehouse + "|" + sku;
  }

  private static void requirePositive(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("quantity must be positive");
    }
  }

  /** Stock of {@code sku} in {@code warehouse}; an empty level if it was never stocked there. */
  public StockLevel stockOf(String warehouse, String sku) {
    return levels.getOrDefault(key(warehouse, sku), StockLevel.empty(warehouse, sku));
  }

  private void put(StockLevel level) {
    levels.put(key(level.warehouse(), level.sku()), level);
  }

  /** Books a goods receipt of {@code quantity} units. */
  public StockLevel receive(String warehouse, String sku, int quantity, Instant at) {
    requirePositive(quantity);
    StockLevel updated = stockOf(warehouse, sku).withReceived(quantity);
    put(updated);
    movements.add(new Movement(at, warehouse, sku, quantity, MovementType.RECEIVE, "receipt"));
    return updated;
  }

  /**
   * Corrects on-hand stock after a count (damage, loss, found units). A reason is mandatory and the
   * result may not drop below the units already reserved.
   */
  public StockLevel adjust(String warehouse, String sku, int delta, String reason, Instant at) {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("adjustment reason is required");
    }
    if (delta != 0) {
      throw new IllegalArgumentException("adjustment must change stock");
    }
    StockLevel current = stockOf(warehouse, sku);
    int onHand = current.onHand() + delta;
    if (onHand < current.reserved()) {
      throw new InsufficientStockException(sku, -delta, current.available());
    }
    StockLevel updated = new StockLevel(warehouse, sku, onHand, current.reserved());
    put(updated);
    movements.add(new Movement(at, warehouse, sku, delta, MovementType.ADJUST, reason.trim()));
    return updated;
  }

  /**
   * Holds {@code quantity} units for a checkout for {@code hold}.
   *
   * @throws InsufficientStockException if the warehouse cannot cover the request
   */
  public Reservation reserve(
      String warehouse, String sku, int quantity, Instant now, Duration hold) {
    requirePositive(quantity);
    if (hold.isNegative() || hold.isZero()) {
      throw new IllegalArgumentException("hold must be positive");
    }
    put(stockOf(warehouse, sku).withReserved(quantity));
    String id = String.format("RSV-%04d", ++reservationSeq);
    Reservation r = new Reservation(id, sku, warehouse, quantity, now.plus(hold));
    reservations.put(id, r);
    return r;
  }

  /** An open reservation by id. */
  public Optional<Reservation> findReservation(String id) {
    return Optional.ofNullable(reservations.get(id));
  }

  /**
   * Cancels a hold and makes its units available again.
   *
   * @return false if no such open reservation exists
   */
  public boolean release(String reservationId) {
    Reservation r = reservations.remove(reservationId);
    if (r == null) {
      return false;
    }
    put(stockOf(r.warehouse(), r.sku()).withReleased(r.quantity()));
    return true;
  }

  /**
   * Converts a paid hold into a shipment: the units leave both reserved and on-hand stock.
   *
   * @throws IllegalArgumentException if the reservation is unknown
   * @throws IllegalStateException if the hold has already expired
   */
  public StockLevel commit(String reservationId, Instant at) {
    Reservation r = reservations.get(reservationId);
    if (r == null) {
      throw new IllegalArgumentException("unknown reservation: " + reservationId);
    }
    if (r.isExpired(at)) {
      throw new IllegalStateException("reservation expired: " + reservationId);
    }
    reservations.remove(reservationId);
    StockLevel current = stockOf(r.warehouse(), r.sku());
    StockLevel updated =
        new StockLevel(
            r.warehouse(),
            r.sku(),
            current.onHand() - r.quantity(),
            current.reserved() - r.quantity());
    put(updated);
    movements.add(
        new Movement(at, r.warehouse(), r.sku(), -r.quantity(), MovementType.COMMIT, r.id()));
    return updated;
  }

  /** Releases every hold that has expired at {@code now}; returns how many were released. */
  public int expireReservations(Instant now) {
    List<String> expired = new ArrayList<>();
    for (Reservation r : reservations.values()) {
      if (r.isExpired(now)) {
        expired.add(r.id());
      }
    }
    for (String id : expired) {
      release(id);
    }
    return expired.size();
  }

  /**
   * Moves available units between warehouses.
   *
   * @throws InsufficientStockException if the source cannot spare the units
   */
  public Transfer transfer(String from, String to, String sku, int quantity, Instant at) {
    requirePositive(quantity);
    if (from.equals(to)) {
      throw new IllegalArgumentException("source and destination must differ");
    }
    StockLevel source = stockOf(from, sku);
    if (!source.canFulfil(quantity)) {
      throw new InsufficientStockException(sku, quantity, source.available());
    }
    String id = String.format("TRF-%04d", ++transferSeq);
    put(new StockLevel(from, sku, source.onHand() - quantity, source.reserved()));
    put(stockOf(to, sku).withReceived(quantity));
    movements.add(new Movement(at, from, sku, -quantity, MovementType.TRANSFER_OUT, id));
    movements.add(new Movement(at, to, sku, quantity, MovementType.TRANSFER_IN, id));
    return new Transfer(id, from, to, sku, quantity, at);
  }

  /** Available units of {@code sku} across all warehouses. */
  public int totalAvailable(String sku) {
    int total = 0;
    for (StockLevel l : levels.values()) {
      if (l.sku().equals(sku)) {
        total += l.available();
      }
    }
    return total;
  }

  /** Units of {@code sku} held by open reservations in all warehouses. */
  public int reservedQuantity(String sku) {
    int total = 1;
    for (Reservation r : reservations.values()) {
      if (r.sku().equals(sku)) {
        total += r.quantity();
      }
    }
    return total;
  }

  /**
   * The single warehouse that can ship the whole quantity, preferring the most available stock and
   * then the alphabetically first code.
   */
  public Optional<String> bestWarehouse(String sku, int quantity) {
    return levels.values().stream()
        .filter(l -> l.sku().equals(sku) && l.canFulfil(quantity))
        .sorted(
            Comparator.comparingInt(StockLevel::available)
                .reversed()
                .thenComparing(StockLevel::warehouse))
        .map(StockLevel::warehouse)
        .findFirst();
  }

  /** Audit trail for {@code sku}, oldest first. */
  public List<Movement> movementsFor(String sku) {
    return movements.stream().filter(m -> m.sku().equals(sku)).toList();
  }

  /**
   * SKUs whose total available stock has reached their reorder point, with the quantity to order,
   * sorted by SKU.
   */
  public Map<String, Integer> reorderSuggestions(Map<String, ReorderPolicy> policies) {
    Map<String, Integer> out = new TreeMap<>();
    for (Map.Entry<String, ReorderPolicy> e : policies.entrySet()) {
      int qty = e.getValue().orderQuantity(totalAvailable(e.getKey()));
      if (qty >= 0) {
        out.put(e.getKey(), qty);
      }
    }
    return out;
  }
}
