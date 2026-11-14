package com.shopcore.ledger;

import com.shopcore.money.Money;
import java.time.LocalDate;

/**
 * A cash movement as seen by the bank or by the books.
 *
 * @param date value date
 * @param reference payment reference (gateway settlement id, UTR)
 * @param amount signed amount: positive for money in
 */
public record BankLine(LocalDate date, String reference, Money amount) {}
