package org.postgresql.sql2.execution;

import java.io.IOException;
import java.nio.channels.Channel;

/**
 * NIO service for the {@link NioLoop}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NioService {

	/**
	 * Handles the connect.
	 * 
	 * @throws IOException If fails to handle the accept.
	 */
	void handleConnect() throws IOException;

	/**
	 * Indicates data is available to read.
	 * 
	 * @throws IOException If failure in reading and processing the data.
	 */
	void handleRead() throws IOException;

	/**
	 * Indicates underlying {@link Channel} has cleared space for further writing.
	 * 
	 * @throws IOException If failure in writing data.
	 */
	void handleWrite() throws IOException;

	/**
	 * Handles a {@link Throwable} in servicing.
	 * 
	 * @param ex {@link Throwable} to be handled.
	 */
	void handleException(Throwable ex);
}
