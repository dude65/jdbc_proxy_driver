package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

import org.fit.proxy.jdbc.ConnectionUnit;

/**
 * Interface to run non-complex action
 * @author Ondřej Marek
 */
public interface ISimpleAction {
	/**
	 * Runs action
	 * @throws SQLException if something goes wrong
	 */
	public void runAction(ConnectionUnit connection) throws SQLException;
	
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
	
	/**
	 * Sometimes the action produce some result
	 * @return action result
	 */
	public Object getResult();
}
