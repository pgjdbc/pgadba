# pgAdba

An implementation of [ADBA](http://cr.openjdk.java.net/%7Elancea/8188051/apidoc/jdk/incubator/sql2/package-summary.html), a proposed asynchronous SQL specification, for PostgreSQL.

## Asynchronous querying with `Future`

The primary difference from JDBC is that with ADBA, query execution is managed using futures. 
When the database returns a result it completes the future, either normally or exceptionally 
if the query had an error.

Compared to plain JDBC, ADBA, this decouples the network communication to the database 
from the business logic that produces queries and consumes the results.

This reduces the number of threads needed in the application, since there isn't any need
for threads to wait for the database to produce results. This in turn reduces memory 
footprint and the amount of context switching needed.

## Structural improvements over JDBC

* No parsing of queries in the connection library. This significantly reduces the amount of
work the library needs to perform.

* All queries are sent as prepared statements, this helps mitigate the problem with SQL 
injection, as it's no longer possible to terminate one query and start a new one in the same
statement. It doesn't solve the SQL injection problem totally of course.

* Query pipelining, the library will send query number 2 over the network without 
waiting for the result of query number 1 if the arguments to query 2 doesn't depend on 
query 1. This helps a great deal in reducing query time in environments where there is 
network latency against the database server.
 

## How does it work

The library's network stack is based on the asynchronous part of the NIO framework.

The programmer interface is a significant rework and upgrade of the old JDBC standard.

The core concepts are `DataSources`, `Sessions` and `Operations`.

* `DataSource` represents the database on the other side of the network, you can start 
`Session`s from it.
* `Session` is a connection to the database, over TCP and optionally TLS in the case of 
Postgresql. `Session`s is where you send in Operations containing SQL queries and get Futures 
back
* Operations represent SQL queries, since there is a large amount of different types of queries,
there is also a number of different operations that perform different tasks.

## Dependency inclusion

### Maven

```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>pgadba</artifactId>
  <version>0.1.0-ALPHA</version>
</dependency>
```

### Gradle

```groovy
compile 'org.postgresql:pgadba:0.1.0-ALPHA'
```

## Examples

### Creating a DataSource

```java
    DataSourceFactory.newFactory("org.postgresql.adba.PgDataSourceFactory")
        .builder()
        .url("jdbc:postgresql://" + postgres.getContainerIpAddress() + ":" + postgres.getMappedPort(5432)
            + "/" + postgres.getDatabaseName())
        .username(postgres.getUsername())
        .password(postgres.getPassword())
        .build();
```

This is a straightforward building pattern.

### Performing a query

```java
    List<Integer> result = new ArrayList<>();
    try (Session session = ds.getSession()) {
      Submission<List<Integer>> sub = session.<List<Integer>>rowOperation("select $1 as id")
          .set("$1", 1, AdbaType.INTEGER)
          .collect(() -> result,
              (list, row) -> list.add(row.at("id").get(Integer.class)))
          .submit();
      get10(sub.getCompletionStage());
    }
```

Lets go over this line by line.

0. Just a list to hold the result of the operation.
1. We start by getting a session in a try-with-resources construct. `getSession()` is a 
convenience function that creates a ConnectionOperation and submits it to the operation 
stack
2. Here we create a row operation, queries that returns rows generally want to use row
operations, while queries that return the number of rows affected should use countOperations
3. Setting of parameters, no parsing of the query is done in the library, so replacing
the $1 with the parameter is done by the server in the same way that prepared statements
work in regular jdbc.
4. The collect call takes a standard java.util.stream.Collector, and have a overloaded 
default implementation that uses Collector.of().
5. `submit()` signals that we are done building the `Operation` and want to send it off
to be executed by the server.
6. get10 is a helper function that waits for a future to complete, with a timeout of 10s.

### Full application example

[Spring Boot Example](https://github.com/alexanderkjall/pgadba-example-application-spring-boot/)

## How can I get involved

This is very much a work in progress. Bail in if interested!

All bug reports, pull requests and suggestions are very welcome.
