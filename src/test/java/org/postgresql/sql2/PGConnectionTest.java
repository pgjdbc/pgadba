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
  public void trivialInsert() throws InterruptedException {

    String sql = "insert into tab(id, name, answer) values ($1, $2, $3)";
    try (Connection conn = ds.getConnection()) {
      conn.countOperation(sql)
          .set("$1", 1, AdbaType.NUMERIC)
          .set("$2", "Deep Thought", AdbaType.VARCHAR)
          .set("$3", 42, AdbaType.NUMERIC)
          .submit();
    }
  }
}