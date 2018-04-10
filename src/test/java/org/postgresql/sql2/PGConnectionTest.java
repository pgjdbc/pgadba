package org.postgresql.sql2;

import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import org.junit.BeforeClass;
import org.junit.Test;

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
          .set("id", 1, AdbaType.NUMERIC)
          .set("name", "Deep Thought", AdbaType.VARCHAR)
          .set("answer", 42, AdbaType.NUMERIC)
          .submit();
    }
  }
}