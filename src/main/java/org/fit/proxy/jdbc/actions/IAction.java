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
	 * @throws ProxyException if something goes wrong
	 */
	public void runReverseAction() throws ProxyException;
	
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
	 * Returns the name of the property that is changed
	 * @return name
	 */
	public String getPropertyName();
	
	/**
	 * Returns the value of the property that is changed
	 * @return value 
	 */
	public Object getPropertyValue();
}
