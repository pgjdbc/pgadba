package org.postgresql.sql2.operations.helpers;

import jdk.incubator.sql2.TransactionCompletion;

public class PgTransaction implements TransactionCompletion {
  private boolean rollbackOnly;

  @Override
  public boolean setRollbackOnly() {
    return rollbackOnly = true;
  }

  @Override
  public boolean isRollbackOnly() {
    return rollbackOnly;
  }
}
