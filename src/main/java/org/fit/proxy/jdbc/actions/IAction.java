package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.exception.ProxyException;

/**
 * Interface to run actions
 * @author Ondřej Marek
 *
 */
public interface IAction extends ISimpleAction {
	
	/**
	 * Runs action that repairs faults caused by runAction()
	 * @throws ProxyException if something goes wrong
	 */
	public void runReverseAction(ConnectionUnit connection, Object value) throws SQLException;
	
	/**
	 * Returns the actual value in specific connection
	 * @param connection connection
	 * @return actual value
	 * @throws SQLException if something goes wrong
	 */
	public Object getSaveValue(ConnectionUnit connection) throws SQLException;
	
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
