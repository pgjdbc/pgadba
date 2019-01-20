package org.postgresql.adba.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import jdk.incubator.sql2.DataSourceProperty;
import jdk.incubator.sql2.SessionProperty;
import org.postgresql.adba.PgDataSourceProperty;
import org.postgresql.adba.PgSessionDbProperty;
import org.postgresql.adba.PgSessionProperty;

/**
 * This class is responsible for keeping properties, and throwing exceptions when trying to add new properties in
 * illegal ways.
 */
public class PropertyHolder {
  private final Map<DataSourceProperty, Object> properties;
  private final Map<SessionProperty, Object> defaultSessionProperties;
  private final Map<SessionProperty, Object> sessionProperties;

  /**
   * Creates a new object.
   */
  public PropertyHolder() {
    properties = new HashMap<>();
    defaultSessionProperties = new HashMap<>();
    sessionProperties = new HashMap<>();
  }

  /**
   * copy constructor.
   * @param holder copies all values from here
   */
  public PropertyHolder(PropertyHolder holder) {
    properties = new HashMap<>(holder.properties);
    defaultSessionProperties = new HashMap<>(holder.defaultSessionProperties);
    sessionProperties = new HashMap<>(holder.sessionProperties);
  }

  /**
   * Set a property on the DataSource level.
   *
   * @param property property to set
   * @param value value, can be null
   */
  public void property(DataSourceProperty property, Object value) {
    properties.put(property, value);
  }

  /**
   * Set a default value for a property on the Session level.
   *
   * @param property property to set
   * @param value value, can be null
   */
  public void defaultSessionProperty(SessionProperty property, Object value) {
    validate(property, value, false);

    add(property, value, defaultSessionProperties);
  }

  /**
   * Set a value for a property on the Session level.
   *
   * @param property property to set
   * @param value value, can be null
   */
  public void sessionProperty(SessionProperty property, Object value) {
    validate(property, value, false);

    add(property, value, sessionProperties);
  }

  /**
   * Registers a new property on the Session level.
   *
   * @param property property to set
   */
  public void registerSessionProperty(SessionProperty property) {
    validate(property, null, false);

    add(property, property.defaultValue(), defaultSessionProperties);
  }

  /**
   * Set a value for a property on the Session level, overrides default values.
   *
   * @param property property to set
   * @param value value, can be null
   */
  public void sessionPropertyFromSessionBuilder(SessionProperty property, Object value) {
    validate(property, value, true);

    add(property, value, sessionProperties);
  }

  public void sessionDbProperty(PgSessionDbProperty property, Object value) {
    add(property, value, sessionProperties);
  }

  /**
   * Adds all PgSessionProperty default values.
   */
  public void addAllPgDefaults() {
    for (PgSessionProperty prop : PgSessionProperty.values()) {
      defaultSessionProperties.putIfAbsent(prop, prop.defaultValue());
    }
  }

  private void add(SessionProperty property, Object value, Map<SessionProperty, Object> toAdd) {
    if (value instanceof Cloneable) {
      try {
        Method clone = value.getClass().getDeclaredMethod("clone");
        toAdd.put(property, clone.invoke(value));
      } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        throw new IllegalStateException("problem with the clone call", e);
      }
    } else {
      toAdd.put(property, value);
    }
  }

  private void validate(SessionProperty property, Object value, boolean overrideDefault) {
    if (property == null) {
      throw new IllegalArgumentException("property object may not be null");
    }

    if (value != null) {
      try {
        if (!property.validate(value)) {
          throw new IllegalArgumentException("value of " + property.name() + " is of the wrong type");
        }
      } catch (Throwable e) {
        if (e instanceof IllegalArgumentException) {
          throw e;
        }
        throw new IllegalStateException("Exception thrown while validating value", e);
      }
    }

    if (overrideDefault) {
      if (sessionProperties.containsKey(property)) {
        throw new IllegalArgumentException("you are not allowed to register the same property twice");
      }
    } else {
      if (sessionProperties.containsKey(property) || defaultSessionProperties.containsKey(property)) {
        throw new IllegalArgumentException("you are not allowed to register the same property twice");
      }
    }
  }

  /**
   * Gets the property value, or it's default value if it's not set.
   *
   * @param property property to get
   * @return set value, or default
   */
  public Object get(PgDataSourceProperty property) {
    return properties.getOrDefault(property, property.defaultValue());
  }

  /**
   * Gets the property value, or it's default value if it's not set.
   *
   * @param property property to get
   * @return set value, or default
   */
  public Object get(SessionProperty property) {
    return sessionProperties.getOrDefault(property, defaultSessionProperties.get(property));
  }

  /**
   * Gets all non-sensitive SessionProperty values as a map.
   *
   * @return all non-sensitive SessionProperty values as a map
   */
  public Map<SessionProperty, Object> getAll() {
    Map<SessionProperty, Object> toReturn = new HashMap<>();

    for (Map.Entry<SessionProperty, Object> entry : defaultSessionProperties.entrySet()) {
      if (entry.getValue() != null && !entry.getKey().isSensitive()) {
        toReturn.put(entry.getKey(), entry.getValue());
      }
    }

    for (Map.Entry<SessionProperty, Object> entry : sessionProperties.entrySet()) {
      if (entry.getValue() != null && !entry.getKey().isSensitive()) {
        toReturn.put(entry.getKey(), entry.getValue());
      }
    }

    return toReturn;
  }
}