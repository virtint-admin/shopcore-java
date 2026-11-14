package com.shopcore.ledger;

import com.shopcore.money.Currency;
import com.shopcore.money.Money;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/** The general ledger: an append-only list of balanced journal entries in one currency. */
public final class Ledger {

  private final ChartOfAccounts chart;
  private final Currency currency;
  private final Map<String, JournalEntry> entries = new LinkedHashMap<>();
  private final Set<String> reversed = new HashSet<>();

  /** An empty ledger booking into {@code chart} in {@code currency}. */
  public Ledger(ChartOfAccounts chart, Currency currency) {
    this.chart = chart;
    this.currency = currency;
  }

  /**
   * Books an entry.
   *
   * @throws IllegalArgumentException if it is unbalanced, reuses an id, uses another currency or an
   *     unknown account
   */
  public void post(JournalEntry entry) {
    if (entries.containsKey(entry.id())) {
      throw new IllegalArgumentException("duplicate entry id " + entry.id());
    }
    if (entry.postings().get(0).amount().currency() != currency) {
      throw new IllegalArgumentException("ledger currency is " + currency);
    }
    if (!entry.isBalanced()) {
      throw new IllegalArgumentException(
          "unbalanced entry "
              + entry.id()
              + ": "
              + entry.totalDebits()
              + " vs "
              + entry.totalCredits());
    }
    for (Posting p : entry.postings()) {
      chart.require(p.accountCode());
    }
    entries.put(entry.id(), entry);
  }

  /** Number of entries booked. */
  public int entryCount() {
    return entries.size();
  }

  /** The entry with {@code id}, if booked. */
  public Optional<JournalEntry> find(String id) {
    return Optional.ofNullable(entries.get(id));
  }

  private Money balance(String code, LocalDate asOf) {
    Account account = chart.require(code);
    long debits = 0;
    long credits = 0;
    for (JournalEntry e : entries.values()) {
      if (asOf != null && e.date().isAfter(asOf)) {
        continue;
      }
      for (Posting p : e.postings()) {
        if (p.accountCode().equals(code)) {
          debits += p.debit().minor();
          credits += p.credit().minor();
        }
      }
    }
    long normal = account.type().isDebitNormal() ? debits - credits : credits - debits;
    return Money.ofMinor(normal, currency);
  }

  /**
   * Balance of an account in its normal direction: positive means a debit balance for assets and
   * expenses and a credit balance for liabilities, equity and revenue.
   */
  public Money balanceOf(String code) {
    return balance(code, null);
  }

  /** {@link #balanceOf} counting only entries dated on or before {@code asOf}. */
  public Money balanceAsOf(String code, LocalDate asOf) {
    return balance(code, asOf);
  }

  /** Normal-direction balance of every account that has postings, by account code. */
  public Map<String, Money> trialBalance() {
    Map<String, Money> out = new TreeMap<>();
    for (JournalEntry e : entries.values()) {
      for (Posting p : e.postings()) {
        out.computeIfAbsent(p.accountCode(), this::balanceOf);
      }
    }
    return out;
  }

  /** Whether the ledger's total debits equal its total credits. */
  public boolean isInBalance() {
    long net = 0;
    for (JournalEntry e : entries.values()) {
      net += e.totalDebits().minor() - e.totalCredits().minor();
    }
    return net == 0;
  }

  /** Entries touching {@code code}, in booking order. */
  public List<JournalEntry> entriesFor(String code) {
    List<JournalEntry> out = new ArrayList<>();
    for (JournalEntry e : entries.values()) {
      if (e.postings().stream().anyMatch(p -> p.accountCode().equals(code))) {
        out.add(e);
      }
    }
    return out;
  }

  /**
   * Books the reversal of an entry.
   *
   * @throws IllegalArgumentException if the entry is unknown
   * @throws IllegalStateException if it was already reversed
   */
  public JournalEntry reverse(String entryId, String newId, LocalDate on) {
    JournalEntry original = entries.get(entryId);
    if (original == null) {
      throw new IllegalArgumentException("unknown entry " + entryId);
    }
    if (reversed.contains(entryId)) {
      throw new IllegalStateException("entry already reversed: " + entryId);
    }
    JournalEntry reversal = original.reversal(newId, on);
    post(reversal);
    reversed.add(entryId);
    return reversal;
  }

  /** Revenue minus expenses for entries dated within {@code [from, to]}. */
  public Money netIncome(LocalDate from, LocalDate to) {
    long net = 0;
    for (JournalEntry e : entries.values()) {
      if (e.date().isBefore(from) || e.date().isAfter(to)) {
        continue;
      }
      for (Posting p : e.postings()) {
        AccountType type = chart.require(p.accountCode()).type();
        if (!type.isBalanceSheet()) {
          net += p.credit().minor() - p.debit().minor();
        }
      }
    }
    return Money.ofMinor(net, currency);
  }
}
