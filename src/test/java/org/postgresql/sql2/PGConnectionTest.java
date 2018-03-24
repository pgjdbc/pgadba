package org.postgresql.sql2;

import java2.sql2.Connection;
import java2.sql2.DataSource;
import java2.sql2.JdbcType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static org.junit.Assert.*;

public class PGConnectionTest {

  private static DataSource ds;

  @BeforeClass
  public static void setUp() throws Exception {
    ds = TestUtil.openDB();

    TestUtil.createTable(ds, "tab",
        "id int", "name varchar(100)", "answer int");
  }


  @Test
  public void trivialInsert() {

    String sql = "insert into tab(id, name, answer) values (?, ?, ?)";
    try (Connection conn = ds.getConnection()) {
      conn.countOperation(sql)
          .set("id", 1, JdbcType.NUMERIC)
          .set("name", "Deep Thought", JdbcType.VARCHAR)
          .set("answer", 42, JdbcType.NUMERIC)
          .submit();
    }
  }
}