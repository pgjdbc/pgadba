
module org.postgresql.sql2 {
  requires java.logging;
  exports org.postgresql.sql2;
  exports jdk.incubator.sql2;
  provides jdk.incubator.sql2.DataSourceFactory with org.postgresql.sql2.PGDataSourceFactory;
}