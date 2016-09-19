package org.fit.proxy.jdbc;

import java.sql.SQLException;

/**
 * Class that implements this interface must ensure that the connection of the class object is still alive.
 * @author Ondřej Marek
 */
public interface IConnectionEnsure {
	/**
	 * method that throws an exception when connections is closed
	 * @throws SQLException if connections are closed
	 */
	public void ensureConnectionIsAlive() throws SQLException;
}
