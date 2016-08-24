package org.fit.proxy.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fit.proxy.jdbc.actions.IAction;
import org.fit.proxy.jdbc.actions.ISimpleAction;
import org.fit.proxy.jdbc.exception.ProxyExceptionUtils;
import org.fit.proxy.jdbc.exception.ProxyException;

/**
 * This class provides actions for ProxyConnection.java
 * @author Ondřej Marek
 *
 */
public class ProxyConnectionEngine {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	
	/**
	 * Map, that contains properties that are set in proxy connection
	 */
	Map<String, ConnectionPropertiesUnit> connectionProperties = new HashMap<>();
	
	/**
	 * Sets property
	 * @param name property name
	 * @param value property value
	 */
	public void setProperty(String name, Object value) {
		ConnectionPropertiesUnit property = connectionProperties.get(name);
		
		if (property == null) {
			property = new ConnectionPropertiesUnit(name);
			connectionProperties.put(name, property);
		}
		
		property.setValue(value);
		
	}
	
	
	/**
	 * Unsets property
	 * @param name property name
	 */
	public void unsetProperty(String name) {
		ConnectionPropertiesUnit property = connectionProperties.get(name);
		
		if (property != null) {
			property.unsetValueSet();
		}
	}
	
	/**
	 * Checks whether property is set
	 * @param name property name
	 * @return whether is property set
	 */
	public boolean isPropertySet(String name) {
		ConnectionPropertiesUnit property = connectionProperties.get(name);
		
		return property != null && property.isValueSet();
	}
	
	/**
	 * Returns a value by given name
	 * @param name property name
	 * @return property value
	 * @throws SQLException if property has not been set, yet.
	 */
	public Object getPropertyValue(String name) throws SQLException {
		if (!isPropertySet(name)) {
			String message = new StringBuilder("Attempting to get property named ").append(name).append(" which has not been set, yet!").toString();
			log.warning(message);
			
			throw new SQLException(message);
		}
		
		return connectionProperties.get(name).getValue();
	}
	
	/**
	 * Runs action for Proxy Connection
	 * @param action
	 * @throws SQLException if something goes wrong
	 */
	public void runAction(IAction action) throws SQLException {
		
		try {
			action.runAction();
			setProperty(action.getPropertyName(), action.getPropertyValue());
			log.fine(action.getOkMessage());
		} catch (SQLException e) {
			try {
				action.runReverseAction();
				log.log(Level.WARNING, action.getErrMessage(), e);
				
				throw e;
			} catch (ProxyException pe) {
				pe.setCause(e);
				
				unsetProperty(action.getPropertyName());
				ProxyExceptionUtils.throwAndLogAsSql(pe, Level.SEVERE);
				
			}
		}
	}
	
	public void runSimpleAction(ISimpleAction action) throws SQLException {
		try {
			action.runAction();
			log.fine(action.getOkMessage());
		} catch (SQLException e) {
			//TODO templates (dependency on proxy exception refactoring)
			String templateErrMesage = action.getErrMessage();
			log.log(Level.WARNING, templateErrMesage, e);
			
			throw new SQLException(templateErrMesage, e);
		}
	}
}
