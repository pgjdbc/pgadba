package org.postgresql.sql2.execution;

import java.nio.channels.Selector;

import jdk.incubator.sql2.Operation;

/**
 * <p>
 * Provides an event loop for servicing communication.
 * <p>
 * This allows plugging in different {@link NioLoop} implementations. For
 * example, the same {@link Selector} can be used for both asynchronous database
 * {@link Operation} and HTTP servicing by the web application.
 * 
 * @author Daniel Sagenschneider
 */
public interface NioLoop {

}