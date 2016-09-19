package org.fit.proxy.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fit.proxy.jdbc.actions.IAction;
import org.fit.proxy.jdbc.actions.ISimpleAction;
import org.fit.proxy.jdbc.configuration.ProxyConstants;
import org.fit.proxy.jdbc.exception.ProxyException;
import org.fit.proxy.jdbc.exception.ProxyExceptionUtils;

/**
 * This class provides actions for ProxyConnection.java
 * @author Ondřej Marek
 *
 */
public class ProxyConnectionEngine implements IConnectionEnsure {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	private final Switcher switcher;
	
	public ProxyConnectionEngine(Switcher switcher) {
		this.switcher = switcher;
	}
	
	private final ProxyProperiesHelper propertiesHelper = new ProxyProperiesHelper(this);
	
	public ProxyProperiesHelper getPropertiesHelper() {
		return propertiesHelper;
	}
	
	/**
	 * method that throws an exception when connections are closed
	 * @throws SQLException if connections are closed
	 */
	@Override
	public void ensureConnectionIsAlive() throws SQLException {
		if (propertiesHelper.isPropertySet(ProxyConstants.CLOSE_CONNECTION)) {
			throw new SQLException("Proxy connection has been already closed!");
		}
	}
	
	public void setDefaultDatabase(String database) throws SQLException {
		ensureConnectionIsAlive();
		switcher.setDefaultDatabase(database);
	}
	
	public void setDefaultDatabase(ConnectionUnit database) throws SQLException {
		ensureConnectionIsAlive();
		switcher.setDefaultDatabase(database);
	}
	
	public void unsetDefaultDatabase() throws SQLException {
		ensureConnectionIsAlive();
		switcher.unsetDefaultDatabase();
	}
	
	public ConnectionUnit getConnectionByName(String name) throws SQLException {
		ensureConnectionIsAlive();
		return switcher.getConnectionByName(name);
	}
	
	public List<ConnectionUnit> getConnectionList() throws SQLException {
		ensureConnectionIsAlive();
		return switcher.getConnectionList();
	}
	
	public ConnectionUnit getConnection(String sql) throws SQLException {
		ensureConnectionIsAlive();
		return switcher.getConnection(sql);
	}
	
	public ConnectionUnit getDefaultConnection() throws SQLException {
		ensureConnectionIsAlive();
		return switcher.getDefaultConnection();
	}
	
	/**
	 * Runs action for Proxy Connection
	 * @param action
	 * @throws ProxyException if something goes wrong
	 */
	public void runAction(IAction action) throws SQLException {
		ensureConnectionIsAlive();
		
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
		ensureConnectionIsAlive();
		runSimpleActionInstance(action);
	}
	
	private void runActionInstance(IAction action, ActionUnit info) throws SQLException {
		for (ConnectionUnit connection : info.getConnectionList()) {
			info.setCurrent(connection);
			
			info.saveValue(connection, action.getSaveValue(connection));
			action.runAction(connection);
		}
		
		propertiesHelper.setProperty(action.getPropertyName(), action.getPropertyValue());
		log.fine(action.getOkMessage());
	}
	
	private void runSimpleActionInstance(ISimpleAction action) throws SQLException {
		SQLException inCaseOfFailure = new SQLException(action.getErrMessage());
		
		for (ConnectionUnit connection : switcher.getConnectionList()) {
			try {
				action.runAction(connection);
			} catch (SQLException e) {
				String message = new StringBuilder("Unable to execute action in connection ").append(connection.getName()).append('.').toString();
				ProxyException pe = new ProxyException(message, connection);
				pe.initCause(e);
				
				inCaseOfFailure.setNextException(pe);
			}
		}
		
		if (ProxyExceptionUtils.actionRevertedSuccessfully(inCaseOfFailure)) {
			log.fine(action.getOkMessage());
		} else {
			ProxyExceptionUtils.logExceptions(inCaseOfFailure, Level.WARNING);
			throw inCaseOfFailure;
		}
		
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
			
			propertiesHelper.unsetProperty(action.getPropertyName());
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
