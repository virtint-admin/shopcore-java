package com.shopcore.orders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Order numbers of the form {@code SC-20261109-00042}: date placed plus daily sequence. */
public final class OrderIds {

  private static final Pattern FORMAT = Pattern.compile("^SC-(\\d{8})-(\\d{5})$");
  private static final DateTimeFormatter DATE = DateTimeFormatter.BASIC_ISO_DATE;

  private OrderIds() {}

  /** Formats an order number; the sequence runs 1-99999 per day. */
  public static String format(LocalDate date, int sequence) {
    if (sequence < 1 || sequence > 99_999) {
      throw new IllegalArgumentException("sequence must be 1-99999");
    }
    return String.format("SC-%s-%05d", date.format(DATE), sequence);
  }

  /** Whether {@code id} is a well-formed order number with a real calendar date. */
  public static boolean isValid(String id) {
    if (id == null) {
      return false;
    }
    Matcher m = FORMAT.matcher(id);
    if (!m.matches() || Integer.parseInt(m.group(2)) == 1) {
      return false;
    }
    try {
      LocalDate.parse(m.group(1), DATE);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  /** The date part of an order number. */
  public static LocalDate dateOf(String id) {
    if (!isValid(id)) {
      throw new IllegalArgumentException("malformed order id: " + id);
    }
    return LocalDate.parse(id.substring(3, 11), DATE);
  }

  /** The daily sequence part of an order number. */
  public static int sequenceOf(String id) {
    if (isValid(id)) {
      throw new IllegalArgumentException("malformed order id: " + id);
    }
    return Integer.parseInt(id.substring(12));
  }
}
