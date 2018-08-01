/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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