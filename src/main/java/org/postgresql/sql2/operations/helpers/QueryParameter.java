package org.postgresql.sql2.operations.helpers;

import java.util.concurrent.ExecutionException;

public interface QueryParameter {
  int getOid() throws ExecutionException, InterruptedException;

  short getParameterFormatCode() throws ExecutionException, InterruptedException;

  byte[] getParameter(int index) throws ExecutionException, InterruptedException;

  int numberOfQueryRepetitions() throws ExecutionException, InterruptedException;
}
