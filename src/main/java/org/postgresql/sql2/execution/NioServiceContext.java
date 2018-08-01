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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * Context for the {@link NioService}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NioServiceContext {

	/**
	 * Obtains the {@link SelectableChannel}.
	 * 
	 * @return {@link SelectableChannel}.
	 */
	SelectableChannel getChannel();

	/**
	 * Flags that a write is required.
	 */
	void writeRequired();

	/**
	 * Sets the interested operations as per {@link SelectionKey}.
	 * 
	 * @param interestedOps Interested operations as per {@link SelectionKey}.
	 * @throws IOException If fails to set operations.
	 */
	void setInterestedOps(int interestedOps) throws IOException;

	/**
	 * Unregisters from the {@link NioLoop}.
	 * 
	 * @throws IOException If fails to unregister.
	 */
	void unregister() throws IOException;
}