package com.shopcore.money;

/** Thrown when two amounts in different currencies are combined without conversion. */
public class CurrencyMismatchException extends IllegalArgumentException {
  private static final long serialVersionUID = 1L;

  /** Creates the exception for the two currencies involved. */
  public CurrencyMismatchException(Currency expected, Currency actual) {
    super("currency mismatch: expected " + expected + " but was " + actual);
  }
}
