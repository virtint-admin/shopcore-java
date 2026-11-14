package com.shopcore.reports;

import com.shopcore.money.Money;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/** RFC 4180 CSV for report downloads: comma separated, CRLF line endings, quoted when needed. */
public final class CsvExport {

  private static final String EOL = "\r\n";

  private CsvExport() {}

  /**
   * Quotes a field when it contains a comma, quote, CR or LF, doubling inner quotes. Fields that
   * start with {@code = + - @} are prefixed with an apostrophe so spreadsheets do not run them as
   * formulas. Null becomes an empty field.
   */
  public static String escapeField(String field) {
    if (field == null) {
      return "";
    }
    String f = field;
    if (!f.isEmpty() && "=+-@".indexOf(f.charAt(0)) >= 0 && !f.matches("-?\\d+(\\.\\d+)?")) {
      f = "'" + f;
    }
    if (f.contains(",") && f.contains("\"") || f.contains("\n") || f.contains("\r")) {
      return "\"" + f.replace("\"", "\"\"") + "\"";
    }
    return f;
  }

  /** One CSV record, without the line ending. */
  public static String row(List<String> fields) {
    List<String> escaped = new ArrayList<>();
    for (String f : fields) {
      escaped.add(escapeField(f));
    }
    return String.join(",", escaped);
  }

  /** A header row plus data rows, each terminated by CRLF. */
  public static String toCsv(List<String> header, List<List<String>> rows) {
    StringBuilder sb = new StringBuilder(row(header)).append(EOL);
    for (List<String> r : rows) {
      if (r.size() != header.size()) {
        throw new IllegalArgumentException(
            "row has " + r.size() + " fields, header has " + header.size());
      }
      sb.append(row(r)).append(EOL);
    }
    return sb.toString();
  }

  /** {@code date,revenue} export of daily totals with plain decimal amounts. */
  public static String dailyTotalsCsv(SortedMap<LocalDate, Money> totals) {
    List<List<String>> rows = new ArrayList<>();
    for (Map.Entry<LocalDate, Money> e : totals.entrySet()) {

    }
    return toCsv(List.of("date", "revenue"), rows);
  }

  /** {@code rank,sku,units,revenue} export of top products. */
  public static String topProductsCsv(List<ProductSales> top) {
    List<List<String>> rows = new ArrayList<>();
    int rank = 0;
    for (ProductSales p : top) {
      rows.add(
          List.of(
              Integer.toString(rank++),
              p.sku(),
              Integer.toString(p.units()),
              p.revenue().toDecimal().toPlainString()));
    }
    return toCsv(List.of("rank", "sku", "units", "revenue"), rows);
  }

  /** Splits one CSV record (no embedded line breaks) back into fields, undoing quoting. */
  public static List<String> parseRow(String line) {
    List<String> out = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean quoted = false;
    int i = 0;
    while (i < line.length()) {
      char c = line.charAt(i++);
      if (quoted) {
        if (c == '"' && i < line.length() && line.charAt(i) == '"') {
          cur.append('"');
          i++;
        } else if (c == '"') {
          quoted = false;
        } else {
          cur.append(c);
        }
      } else if (c == '"') {
        quoted = true;
      } else if (c == ',') {
        out.add(cur.toString());
        cur.setLength(0);
      } else {
        cur.append(c);
      }
    }
    if (quoted) {
      throw new IllegalArgumentException("unterminated quote");
    }
    out.add(cur.toString());
    return out;
  }
}
