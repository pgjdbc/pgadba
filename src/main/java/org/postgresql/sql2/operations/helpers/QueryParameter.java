package org.postgresql.sql2.operations.helpers;

import java.util.concurrent.ExecutionException;

public interface QueryParameter {
  int getOID() throws ExecutionException, InterruptedException;

  short getParameterFormatCode() throws ExecutionException, InterruptedException;

  byte[] getParameter() throws ExecutionException, InterruptedException;
}
