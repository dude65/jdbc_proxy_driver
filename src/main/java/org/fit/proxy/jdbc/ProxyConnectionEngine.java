package org.fit.proxy.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fit.proxy.jdbc.actions.IAction;
import org.fit.proxy.jdbc.actions.ISimpleAction;
import org.fit.proxy.jdbc.exception.ProxyException;
import org.fit.proxy.jdbc.exception.ProxyExceptionUtils;

/**
 * This class provides actions for ProxyConnection.java
 * @author Ondřej Marek
 *
 */
public class ProxyConnectionEngine {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	private final Switcher switcher;
	
	public ProxyConnectionEngine(Switcher switcher) {
		this.switcher = switcher;
	}

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
	 * Checks if the property was even initiated
	 * @param name property name
	 * @return whether property was initiated
	 */
	public boolean isPropertyInitiated(String name) {
		return connectionProperties.containsKey(name);
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
	 * @throws ProxyException if something goes wrong
	 */
	public void runAction(IAction action) throws ProxyException {
		ActionUnit actionInfo = new ActionUnit(switcher);
		
		try {
			runActionInstance(action, actionInfo);
		} catch (SQLException e) {
			ProxyException toThrow = new ProxyException(action.getErrMessage(), e, actionInfo.getCurrent());
			
			revertActionInstance(action, actionInfo, toThrow, e);
			checkRevertingAction(action, actionInfo);
			
			ProxyExceptionUtils.logExceptions(toThrow, Level.WARNING);
			throw toThrow;
		}
	}
	
	public void runSimpleAction(ISimpleAction action) throws SQLException {
		ActionUnit actionInfo = new ActionUnit(switcher);
		
		try {
			runSimpleActionInstance(action, actionInfo);
		} catch (SQLException e) {
			String errMesage = action.getErrMessage();
			log.log(Level.WARNING, errMesage, e);
			
			throw new SQLException(errMesage, e);
		}
	}
	
	private void runActionInstance(IAction action, ActionUnit info) throws SQLException {
		for (ConnectionUnit connection : info.getConnectionList()) {
			info.setCurrent(connection);
			
			info.saveValue(connection, action.getSaveValue(connection));
			action.runAction(connection);
		}
		
		setProperty(action.getPropertyName(), action.getPropertyValue());
		log.fine(action.getOkMessage());
	}
	
	private void runSimpleActionInstance(ISimpleAction action, ActionUnit info) throws SQLException {
		for (ConnectionUnit connection : info.getConnectionList()) {
			info.setCurrent(connection);
			
			action.runAction(connection);
		}
		
		log.fine(action.getOkMessage());
	}
	
	private void revertActionInstance(IAction action, ActionUnit info, ProxyException toThrow, Exception toLog) {
		for (Entry<ConnectionUnit, Object> entry : info.getSaveMap().entrySet()) {
			ConnectionUnit connection = entry.getKey();
			Object value = entry.getValue();
			
			try {
				action.runReverseAction(connection, value);
			} catch (SQLException reverseException) {
				info.saveFailValue(connection, value);
				toThrow.setNextException(reverseException);
			}
		}
		
		log.log(Level.WARNING, action.getErrMessage(), toLog);
	}
	
	private void checkRevertingAction(IAction action, ActionUnit info) {
		Map<ConnectionUnit, Object> revertFail = info.getRevertFail();
		
		if (! revertFail.isEmpty()) {
			StringBuilder sb = new StringBuilder("An error occurred when attempting to recover former values of ").
					append(action.getPropertyName()).append(" to values: ");
		
			for (Entry<ConnectionUnit, Object> entry : revertFail.entrySet()) {
				sb.append('(').append(entry.getKey().getName()).append(',').append(entry.getValue().toString()).append(')');
			}
			
			unsetProperty(action.getPropertyName());
			log.severe(sb.toString());
		}
	}
	
	/**
	 * Help class for actions
	 * @author Ondřej Marek
	 */
	private static final class ActionUnit {
		private ConnectionUnit current;
		private final List<ConnectionUnit> connectionList;
		private final Map<ConnectionUnit, Object> saveMap = new HashMap<>();
		private final Map<ConnectionUnit, Object> revertFail = new HashMap<>();
		
		private ActionUnit(Switcher switcher) {
			connectionList = switcher.getConnectionList();
		}
		
		public ConnectionUnit getCurrent() {
			return current;
		}
		
		public void setCurrent(ConnectionUnit current) {
			this.current = current;
		}
		
		public Map<ConnectionUnit, Object> getSaveMap() {
			return saveMap;
		}
		
		public Map<ConnectionUnit, Object> getRevertFail() {
			return revertFail;
		}
		
		public void saveValue(ConnectionUnit connection, Object value) {
			saveMap.put(connection, value);
		}
		
		public void saveFailValue(ConnectionUnit connection, Object value) {
			revertFail.put(connection, value);
		}

		public List<ConnectionUnit> getConnectionList() {
			return connectionList;
		}
	}
}
