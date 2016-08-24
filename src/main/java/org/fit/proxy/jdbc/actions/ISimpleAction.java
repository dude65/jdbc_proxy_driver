package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

public interface ISimpleAction {
	/**
	 * Runs action
	 * @throws SQLException if something goes wrong
	 */
	public void runAction() throws SQLException;
	
	/**
	 * Gets successful state message
	 * @return message
	 */
	public String getOkMessage();
	
	/**
	 * Gets failure state message
	 * @return message
	 */
	public String getErrMessage();
}
