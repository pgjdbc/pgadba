package org.postgresql.sql2.operations.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class ParameterHolder {
  private Map<Integer, QueryParameter> parameterMap = new TreeMap<>();

  public short size() {
    return (short) parameterMap.size();
  }

  public Collection<QueryParameter> parameters() {
    return parameterMap.values();
  }

  public void add(String id, QueryParameter queryParameter) {
    parameterMap.put(Integer.parseInt(id.substring(1)), queryParameter);
  }

  public List<Integer> getParamTypes() throws ExecutionException, InterruptedException {
    List<Integer> types = new ArrayList<>();

    for (Map.Entry<Integer, QueryParameter> entry : parameterMap.entrySet()) {
      types.add(entry.getValue().getOID());
    }

    return types;
  }

  public int numberOfQueryRepetitions() throws ExecutionException, InterruptedException {
    if (parameterMap.size() == 0) {
      return 1;
    }

    return parameterMap.entrySet().iterator().next().getValue().numberOfQueryRepetitions();
  }
}
