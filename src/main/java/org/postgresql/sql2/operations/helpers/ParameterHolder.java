package org.postgresql.sql2.operations.helpers;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class ParameterHolder {
  Map<Integer, QueryParameter> parameterMap = new TreeMap<>();

  public short size() {
    return (short) parameterMap.size();
  }

  public Collection<QueryParameter> parameters() {
    return parameterMap.values();
  }

  public void add(String id, QueryParameter queryParameter) {
    parameterMap.put(Integer.parseInt(id.substring(1)), queryParameter);
  }
}
