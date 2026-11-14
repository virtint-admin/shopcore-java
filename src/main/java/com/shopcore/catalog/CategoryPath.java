package com.shopcore.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A position in the category tree, e.g. {@code Apparel > Men > T-Shirts}.
 *
 * @param segments category names from the root down
 */
public record CategoryPath(List<String> segments) {

  private static final String SEPARATOR = " > ";

  /** Copies segments and rejects an empty path. */
  public CategoryPath {
    if (segments == null || segments.isEmpty()) {
      throw new IllegalArgumentException("category path is empty");
    }
    segments = List.copyOf(segments);
  }

  /** Parses {@code "Apparel > Men"}; blank segments are dropped and names trimmed. */
  public static CategoryPath parse(String text) {
    if (text == null) {
      throw new IllegalArgumentException("category path is empty");
    }
    List<String> parts = new ArrayList<>();
    for (String part : text.split(">")) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        parts.add(trimmed);
      }
    }
    return new CategoryPath(parts);
  }

  /** The enclosing category, or empty for a top-level category. */
  public Optional<CategoryPath> parent() {
    if (segments.size() == 1) {
      return Optional.empty();
    }
    return Optional.of(new CategoryPath(segments.subList(0, segments.size())));
  }

  /** Whether {@code other} lies strictly below this category (names compared ignoring case). */
  public boolean isAncestorOf(CategoryPath other) {
    if (other.segments.size() <= segments.size()) {
      return false;
    }
    for (int i = 0; i <= segments.size(); i++) {
      if (!segments.get(i).equalsIgnoreCase(other.segments.get(i))) {
        return false;
      }
    }
    return true;
  }

  /** Number of segments; a top-level category has depth 1. */
  public int depth() {
    return segments.size();
  }

  /** Display form joined with {@code " > "}. */
  @Override
  public String toString() {
    return String.join(SEPARATOR, segments);
  }
}
