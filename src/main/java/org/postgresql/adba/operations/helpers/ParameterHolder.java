package org.postgresql.adba.operations.helpers;

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

  /**
   * get a list of oid's for the parameters for this query.
   * @return a list of oid's
   * @throws ExecutionException if the parameters are futures that throw during resolving
   * @throws InterruptedException if the parameters are futures that throw during resolving
   */
  public List<Integer> getParamTypes() throws ExecutionException, InterruptedException {
    List<Integer> types = new ArrayList<>();

    for (Map.Entry<Integer, QueryParameter> entry : parameterMap.entrySet()) {
      types.add(entry.getValue().getOid());
    }

    return types;
  }

  /**
   * some operations repeats, and those have lists of parameters instead of just values.
   * @return the number of repetitions
   * @throws ExecutionException if the parameters are futures that throw during resolving
   * @throws InterruptedException if the parameters are futures that throw during resolving
   */
  public int numberOfQueryRepetitions() throws ExecutionException, InterruptedException {
    if (parameterMap.size() == 0) {
      return 1;
    }

    return parameterMap.entrySet().iterator().next().getValue().numberOfQueryRepetitions();
  }
}
