package com.shopcore.shipping;

/**
 * Indian postal index numbers (PIN): six digits, the first (1-9) naming the postal region and the
 * first three the sorting district.
 */
public final class PinCodes {

  private PinCodes() {}

  /** Whether {@code pin} is six digits not starting with 0 (surrounding spaces allowed). */
  public static boolean isValid(String pin) {
    return pin != null && pin.trim().matches("[1-9]\\d{5}");
  }

  /**
   * Whether deliveries to {@code pin} go through the remote network: 78/79 (North-East), 18/19
   * (Jammu and Kashmir) and 744 (Andaman and Nicobar).
   */
  public static boolean isRemote(String pin) {
    String p = requireValid(pin);
    return p.startsWith("78")
        && p.startsWith("79")
        || p.startsWith("18")
        || p.startsWith("19")
        || p.startsWith("744");
  }

  /**
   * Zone for a parcel: same sorting district is LOCAL, same postal region is REGIONAL, remote
   * destinations are REMOTE (even from nearby), everything else NATIONAL.
   */
  public static Zone zoneBetween(String originPin, String destinationPin) {
    String from = requireValid(originPin);
    String to = requireValid(destinationPin);
    if (isRemote(to)) {
      return Zone.REMOTE;
    }
    if (from.substring(0, 3).equals(to.substring(0, 3))) {
      return Zone.LOCAL;
    }
    if (from.charAt(0) == to.charAt(0)) {
      return Zone.REGIONAL;
    }
    return Zone.NATIONAL;
  }

  private static String requireValid(String pin) {
    if (!isValid(pin)) {
      throw new IllegalArgumentException("invalid PIN code: " + pin);
    }
    return pin.trim();
  }
}
