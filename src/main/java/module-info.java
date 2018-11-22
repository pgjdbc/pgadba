import org.postgresql.adba.PgDataSourceFactory;

module org.postgresql.adba {
  requires java.logging;
  requires client;
  requires common;
  requires java.security.sasl;
  exports org.postgresql.adba;
  exports jdk.incubator.sql2;
  exports org.postgresql.adba.buffer;
  exports org.postgresql.adba.execution;
  provides jdk.incubator.sql2.DataSourceFactory with PgDataSourceFactory;
  uses jdk.incubator.sql2.DataSourceFactory;
}
