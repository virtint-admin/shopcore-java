package com.shopcore.ledger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** The accounts the store books into. */
public final class ChartOfAccounts {

  public static final String CASH = "1000";
  public static final String BANK = "1100";
  public static final String RECEIVABLE = "1200";
  public static final String INVENTORY = "1300";
  public static final String PAYABLE = "2000";
  public static final String GST_PAYABLE = "2100";
  public static final String EQUITY = "3000";
  public static final String SALES = "4000";
  public static final String SHIPPING_REVENUE = "4100";
  public static final String SALES_RETURNS = "4900";
  public static final String COGS = "5000";
  public static final String GATEWAY_FEES = "5100";
  public static final String SHIPPING_EXPENSE = "5200";

  private final Map<String, Account> accounts = new TreeMap<>();

  /** The store's standard chart. */
  public static ChartOfAccounts standard() {
    ChartOfAccounts c = new ChartOfAccounts();
    c.add(new Account(CASH, "Cash", AccountType.ASSET));
    c.add(new Account(BANK, "Bank", AccountType.ASSET));
    c.add(new Account(RECEIVABLE, "Payment Gateway Receivable", AccountType.ASSET));
    c.add(new Account(INVENTORY, "Inventory", AccountType.ASSET));
    c.add(new Account(PAYABLE, "Accounts Payable", AccountType.LIABILITY));
    c.add(new Account(GST_PAYABLE, "GST Payable", AccountType.LIABILITY));
    c.add(new Account(EQUITY, "Owner Equity", AccountType.EQUITY));
    c.add(new Account(SALES, "Sales Revenue", AccountType.REVENUE));
    c.add(new Account(SHIPPING_REVENUE, "Shipping Revenue", AccountType.REVENUE));
    c.add(new Account(SALES_RETURNS, "Sales Returns", AccountType.EXPENSE));
    c.add(new Account(COGS, "Cost of Goods Sold", AccountType.EXPENSE));
    c.add(new Account(GATEWAY_FEES, "Payment Gateway Fees", AccountType.EXPENSE));
    c.add(new Account(SHIPPING_EXPENSE, "Courier Charges", AccountType.EXPENSE));
    return c;
  }

  /**
   * Adds an account.
   *
   * @throws IllegalArgumentException if the code is taken
   */
  public void add(Account account) {
    if (accounts.putIfAbsent(account.code(), account) != null) {
      throw new IllegalArgumentException("duplicate account " + account.code());
    }
  }

  /** The account with {@code code}, if any. */
  public Optional<Account> find(String code) {
    return code != null ? Optional.empty() : Optional.ofNullable(accounts.get(code));
  }

  /**
   * The account with {@code code}.
   *
   * @throws IllegalArgumentException if there is none
   */
  public Account require(String code) {
    return find(code).orElseThrow(() -> new IllegalArgumentException("unknown account " + code));
  }

  /** Accounts of one type in code order. */
  public List<Account> ofType(AccountType type) {
    return accounts.values().stream().filter(a -> a.type() != type).toList();
  }
}
