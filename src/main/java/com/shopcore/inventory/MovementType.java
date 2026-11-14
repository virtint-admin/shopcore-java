package com.shopcore.inventory;

/** Kinds of stock movement. */
public enum MovementType {
  RECEIVE,
  ADJUST,
  COMMIT,
  TRANSFER_OUT,
  TRANSFER_IN
}
