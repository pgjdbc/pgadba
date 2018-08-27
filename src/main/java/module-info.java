import org.postgresql.sql2.PgDataSourceFactory;

module org.postgresql.sql2 {
  requires java.logging;
  exports org.postgresql.sql2;
  exports jdk.incubator.sql2;
  exports org.postgresql.sql2.buffer;
  exports org.postgresql.sql2.execution;
  provides jdk.incubator.sql2.DataSourceFactory with PgDataSourceFactory;
}