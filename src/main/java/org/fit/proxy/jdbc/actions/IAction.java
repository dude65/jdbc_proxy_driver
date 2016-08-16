package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

/**
 * Interface to run actions
 * @author Ondřej Marek
 *
 */
public interface IAction {
	
	/**
	 * Runs action
	 * @throws SQLException if something goes wrong
	 */
	public void run() throws SQLException;
}
