package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

import org.fit.proxy.jdbc.exception.ProxyException;

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
	public void runAction() throws SQLException;
	
	/**
	 * Runs action that repairs faults caused by runAction()
	 * @throws ProxyException
	 */
	public void runReverseAction() throws ProxyException;
}
