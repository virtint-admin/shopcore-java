package com.shopcore.ledger;

import static com.shopcore.ledger.ChartOfAccounts.BANK;
import static com.shopcore.ledger.ChartOfAccounts.COGS;
import static com.shopcore.ledger.ChartOfAccounts.GATEWAY_FEES;
import static com.shopcore.ledger.ChartOfAccounts.GST_PAYABLE;
import static com.shopcore.ledger.ChartOfAccounts.INVENTORY;
import static com.shopcore.ledger.ChartOfAccounts.RECEIVABLE;
import static com.shopcore.ledger.ChartOfAccounts.SALES;
import static com.shopcore.ledger.ChartOfAccounts.SALES_RETURNS;
import static com.shopcore.ledger.ChartOfAccounts.SHIPPING_REVENUE;

import com.shopcore.money.Money;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Standard journal entries for store events. Zero-amount legs are left out. */
public final class Postings {

  private Postings() {}

  /**
   * An order is placed and charged to the payment gateway: debit the receivable for the gross
   * amount; credit sales (net of tax and shipping), GST payable and shipping revenue.
   */
  public static JournalEntry sale(
      String id, LocalDate date, Money gross, Money tax, Money shipping) {
    Money merchandise = gross.minus(tax).minus(shipping);
    if (merchandise.isNegative()) {
      throw new IllegalArgumentException("tax and shipping exceed the gross amount");
    }
    List<Posting> ps = new ArrayList<>();
    ps.add(Posting.ofDebit(RECEIVABLE, gross));
    if (!merchandise.isZero()) {
      ps.add(Posting.ofCredit(SALES, merchandise));
    }
    if (!tax.isZero()) {
      ps.add(Posting.ofCredit(GST_PAYABLE, tax));
    }
    if (!shipping.isZero()) {
      ps.add(Posting.ofCredit(SHIPPING_REVENUE, shipping));
    }
    return new JournalEntry(id, date, "Sale " + id, ps);
  }

  /**
   * The gateway settles: the bank receives the amount less the gateway fee, the fee is expensed and
   * the receivable is cleared in full.
   */
  public static JournalEntry settlement(String id, LocalDate date, Money amount, Money fee) {
    if (fee.isNegative() || fee.isGreaterThan(amount)) {
      throw new IllegalArgumentException("fee must be between zero and the amount");
    }
    List<Posting> ps = new ArrayList<>();
    if (!amount.minus(fee).isZero()) {
      ps.add(Posting.ofDebit(BANK, amount.minus(fee)));
    }
    if (!fee.isZero()) {
      ps.add(Posting.ofDebit(GATEWAY_FEES, fee));
    }
    ps.add(Posting.ofCredit(RECEIVABLE, amount));
    return new JournalEntry(id, date, "Settlement " + id, ps);
  }

  /** A refund to the customer: reverse the sale value and the GST, paid out of the bank. */
  public static JournalEntry refund(String id, LocalDate date, Money net, Money tax) {
    List<Posting> ps = new ArrayList<>();
    if (!net.isZero()) {
      ps.add(Posting.ofDebit(SALES_RETURNS, net));
    }
    if (!tax.isZero()) {
      ps.add(Posting.ofDebit(GST_PAYABLE, tax));
    }
    ps.add(Posting.ofCredit(BANK, net.plus(tax)));
    return new JournalEntry(id, date, "Refund " + id, ps);
  }

  /** Goods leave the warehouse: move their cost from inventory to cost of goods sold. */
  public static JournalEntry costOfSale(String id, LocalDate date, Money cost) {
    return new JournalEntry(
        id,
        date,
        "Cost of sale " + id,
        List.of(Posting.ofDebit(COGS, cost), Posting.ofCredit(INVENTORY, cost)));
  }
}
