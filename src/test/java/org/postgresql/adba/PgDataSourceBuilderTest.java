package org.postgresql.adba;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jdk.incubator.sql2.SessionProperty;
import org.junit.jupiter.api.Test;

public class PgDataSourceBuilderTest {

  @Test
  public void registerSessionPropertyWithNull() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    assertThrows(IllegalArgumentException.class, () -> builder.registerSessionProperty(null));
  }

  @Test
  public void registerSessionPropertyTwice() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.registerSessionProperty(p);
    assertThrows(IllegalArgumentException.class, () -> builder.registerSessionProperty(p));
  }

  @Test
  public void registerSessionPropertyAfterDefaultValueIsSet() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.defaultSessionProperty(p, "test");
    assertThrows(IllegalArgumentException.class, () -> builder.registerSessionProperty(p));
  }

  @Test
  public void registerSessionPropertyAfterSessionPropertyValueIsSet() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.sessionProperty(p, "test");
    assertThrows(IllegalArgumentException.class, () -> builder.registerSessionProperty(p));
  }

  @Test
  public void registerSessionPropertyAfterBuild() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.build();
    assertThrows(IllegalStateException.class, () -> builder.registerSessionProperty(p));
  }

  @Test
  public void sessionPropertyWithNull() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    assertThrows(IllegalArgumentException.class, () -> builder.sessionProperty(null, null));
  }

  @Test
  public void sessionPropertyWrongType() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    assertThrows(IllegalArgumentException.class, () -> builder.sessionProperty(new TestSessionProperty(), 123));
  }

  @Test
  public void sessionPropertyCloneable() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    CloneableValue value = new CloneableValue();

    builder.sessionProperty(new TestCloneableValueSessionProperty(), value);

    assertTrue(value.isCloneCalled());
  }

  @Test
  public void sessionPropertyExceptional() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    RuntimeException e = new RuntimeException("my exception");

    try {
      builder.sessionProperty(new ExceptionalSessionProperty(e), "");
    } catch (IllegalStateException e2) {
      assertEquals(e, e2.getCause());
    }
  }

  @Test
  public void sessionPropertyTwice() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.sessionProperty(p, "");
    assertThrows(IllegalArgumentException.class, () -> builder.sessionProperty(p, ""));
  }

  @Test
  public void sessionPropertyAfterRegisterSessionProperty() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.registerSessionProperty(p);
    assertThrows(IllegalArgumentException.class, () -> builder.sessionProperty(p, ""));
  }

  @Test
  public void sessionPropertyAfterDefaultSessionProperty() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.defaultSessionProperty(p, "");
    assertThrows(IllegalArgumentException.class, () -> builder.sessionProperty(p, ""));
  }

  @Test
  public void sessionPropertyAfterBuild() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.build();
    assertThrows(IllegalStateException.class, () -> builder.sessionProperty(p, ""));
  }


  @Test
  public void defaultSessionPropertyWithNull() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    assertThrows(IllegalArgumentException.class, () -> builder.defaultSessionProperty(null, null));
  }

  @Test
  public void defaultSessionPropertyWrongType() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    assertThrows(IllegalArgumentException.class, () -> builder.defaultSessionProperty(new TestSessionProperty(), 123));
  }

  @Test
  public void defaultSessionPropertyCloneable() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    CloneableValue value = new CloneableValue();

    builder.defaultSessionProperty(new TestCloneableValueSessionProperty(), value);

    assertTrue(value.isCloneCalled());
  }

  @Test
  public void defaultSessionPropertyExceptional() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    RuntimeException e = new RuntimeException("my exception");

    try {
      builder.defaultSessionProperty(new ExceptionalSessionProperty(e), "");
    } catch (IllegalStateException e2) {
      assertEquals(e, e2.getCause());
    }
  }

  @Test
  public void defaultSessionPropertyTwice() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.defaultSessionProperty(p, "");
    assertThrows(IllegalArgumentException.class, () -> builder.defaultSessionProperty(p, ""));
  }

  @Test
  public void defaultSessionPropertyAfterRegisterSessionProperty() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.registerSessionProperty(p);
    assertThrows(IllegalArgumentException.class, () -> builder.defaultSessionProperty(p, ""));
  }

  @Test
  public void defaultSessionPropertyAfterSessionProperty() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.sessionProperty(p, "");
    assertThrows(IllegalArgumentException.class, () -> builder.defaultSessionProperty(p, ""));
  }

  @Test
  public void defaultSessionPropertyAfterBuild() {
    PgDataSourceBuilder builder = new PgDataSourceBuilder();

    TestSessionProperty p = new TestSessionProperty();

    builder.build();
    assertThrows(IllegalStateException.class, () -> builder.defaultSessionProperty(p, ""));
  }

  private class TestSessionProperty implements SessionProperty {

    @Override
    public String name() {
      return "test property name";
    }

    @Override
    public Class<?> range() {
      return String.class;
    }

    @Override
    public Object defaultValue() {
      return "default";
    }

    @Override
    public boolean isSensitive() {
      return false;
    }
  }

  private class CloneableValue implements Cloneable {
    private boolean cloneCalled = false;

    @Override
    public Object clone() {
      cloneCalled = true;
      return this;
    }

    public boolean isCloneCalled() {
      return cloneCalled;
    }
  }

  private class TestCloneableValueSessionProperty implements SessionProperty {

    @Override
    public String name() {
      return "CloneableValue property name";
    }

    @Override
    public Class<?> range() {
      return CloneableValue.class;
    }

    @Override
    public Object defaultValue() {
      return null;
    }

    @Override
    public boolean isSensitive() {
      return false;
    }
  }

  private class ExceptionalSessionProperty implements SessionProperty {
    private RuntimeException e;

    private ExceptionalSessionProperty(RuntimeException e) {
      this.e = e;
    }

    @Override
    public String name() {
      return "test property name";
    }

    @Override
    public Class<?> range() {
      throw e;
    }

    @Override
    public Object defaultValue() {
      return "default";
    }

    @Override
    public boolean isSensitive() {
      return false;
    }
  }

}
