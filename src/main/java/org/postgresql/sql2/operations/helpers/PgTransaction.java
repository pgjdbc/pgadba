package org.postgresql.sql2.operations.helpers;

import jdk.incubator.sql2.Transaction;

public class PgTransaction implements Transaction {
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
