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

  /**
   * When the user adds parameters they gets stored by this method.
   *
   * @param id parameter id string, on the format $1
   * @param queryParameter the parameter, either the value or an future
   */
  public void add(String id, QueryParameter queryParameter) {
    try {
      parameterMap.put(Integer.parseInt(id.substring(1)), queryParameter);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("the names of parameter placeholders must be on the format: "
          + "$<number> with the numbers starting at 1 and increasing by 1 for each parameter", e);
    }
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
