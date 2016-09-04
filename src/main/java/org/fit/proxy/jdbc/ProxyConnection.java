package org.fit.proxy.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fit.proxy.jdbc.actions.AbortAction;
import org.fit.proxy.jdbc.actions.AutoCommitAction;
import org.fit.proxy.jdbc.actions.CatalogAction;
import org.fit.proxy.jdbc.actions.ClearWarningsAction;
import org.fit.proxy.jdbc.actions.GetWarningsAction;
import org.fit.proxy.jdbc.actions.ISimpleAction;
import org.fit.proxy.jdbc.actions.NetworkTimeoutAction;
import org.fit.proxy.jdbc.actions.ReadOnlyAction;
import org.fit.proxy.jdbc.actions.SchemaAction;
import org.fit.proxy.jdbc.actions.TypeMapAction;
import org.fit.proxy.jdbc.configuration.ProxyConstants;

/**
 * 
 * @author Ondřej Marek
 * 
 * This class is the implementation of the Proxy Connection
 *
 */
public class ProxyConnection implements Connection {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	
	private Switcher switcher;
	private final ProxyConnectionEngine engine;
	
	private ProxySavepoint currTransaction;
	
	public ProxyConnection(Switcher s) throws SQLException {
		switcher = s;
		engine = new ProxyConnectionEngine(switcher);
	}
	
	/**
	 * Returns connection specified by name.
	 * 
	 * @param name of connection
	 * @return connection by name
	 * @throws SQLException - name of connection does not match to any connection
	 */
	public Connection getConnectionByName(String name) throws SQLException {
		return switcher.getConnectionByName(name);
	}
	
	/**
	 * Sets default database connection specified by name. When an exception is thrown then the default connection will not change.
	 * 
	 * @param name of connection
	 * @throws SQLException - name of connection does not match to any connection
	 */
	public void setDefaultDatabase(String name) throws SQLException {
		switcher.setDefaultDatabase(name);
	}
	
	/**
	 * Sets default database connection to null.
	 */
	public void unsetDefaultDatabase() {
		switcher.unsetDefaultDatabase();
	}
	
	/**
	 * Returns the list of failed connections from the last time, when exception was thrown. It is useful when handling exception.
	 * 
	 * @return - the list of failed connections (new object), if no connection ever failed, then returns empty list
	 */
	@Deprecated
	public List<Connection> getFailedConnections() {
		return switcher.getFailedConnections();
	}
	
	/**
	 * Releases all savepoints that were used during transactions
	 * 
	 * @param map of savepoints
	 * @throws SQLException - message of unreleased savepoints.
	 */
	private void releaseSavepoint(Map<ConnectionUnit, Savepoint> save) throws SQLException {
		String exc = new String();
		boolean first = true;
		
		log.log(Level.FINE, "Releasing savepoints");
		
		for (Entry<ConnectionUnit, Savepoint> entry : save.entrySet()) {
			ConnectionUnit u = entry.getKey();
			Savepoint s = entry.getValue();
			
			log.log(Level.FINE, "Releasing savepoint with ID = " + s.getSavepointId() + "in connection " + u.getName());
			
			try {
				u.getConnection().releaseSavepoint(s);
			} catch (SQLException e) {
				if (first) {
					first = false;
				} else {
					exc += '\n';
				}
				
				String message = "Unable to release savepoint to connection " + u.getName() + ". Original message: " + e.getMessage();
				
				log.log(Level.SEVERE, message);
				exc += message;
			}
			
		}
		
		if (! exc.isEmpty()) {
			throw new SQLException(exc);
		}
	}
	
	public Switcher getSwitcher() {		
		return switcher;
	}
	
	//Override methods
	
	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		log.log(Level.FINE, "Prepare statement, sql(" + sql + ")");
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		log.log(Level.FINE, "Prepare call, sql(" + sql + ")");
		Connection c = switcher.getConnection(sql);
		return c.prepareCall(sql);
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		log.log(Level.FINE, "Prepare statement, sql(" + sql + "), resultSetType = " + resultSetType + ", resultSetConcurrency = " + resultSetConcurrency);
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		log.log(Level.FINE, "Prepare call, sql(" + sql + "), resultSetType = " + resultSetType + ", resultSetConcurrency = " + resultSetConcurrency);
		Connection c = switcher.getConnection(sql);
		return c.prepareCall(sql, resultSetType, resultSetConcurrency);
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		log.log(Level.FINE, "Prepare statement, sql(" + sql + "), resultSetType = " + resultSetType + ", resultSetConcurrency = " + resultSetConcurrency + ", resultSetHoldability = " + resultSetHoldability);		
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		log.log(Level.FINE, "Prepare call, sql(" + sql + "), resultSetType = " + resultSetType + ", resultSetConcurrency = " + resultSetConcurrency + ", resultSetHoldability = " + resultSetHoldability);
		Connection c = switcher.getConnection(sql);
		return c.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		log.log(Level.FINE, "Prepare statement, sql(" + sql + "), autoGeneratedKeys = " + autoGeneratedKeys);
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		log.log(Level.FINE, "Prepare statement, sql(" + sql + "), columnIndexes = " + columnIndexes);
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		log.log(Level.FINE, "Prepare statement, sql(" + sql + "), columnNames = " + columnNames);
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql, columnNames);
	}
	
	@Override
	public void close() throws SQLException {
		switcher.closeConnections();
	}
	
	@Override
	public boolean isClosed() throws SQLException {
		return switcher.isClosed();
	}
	
	@Override
	public String nativeSQL(String sql) throws SQLException {
		log.log(Level.FINE, "Getting native sql(" + sql + ")");
		Connection c = switcher.getConnection(sql);
		return c.nativeSQL(sql);
	}
	
	@Override
	public Statement createStatement() throws SQLException {
		return new ProxyStatement(this);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return new ProxyStatement(this, resultSetType, resultSetConcurrency);
	}
	
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return new ProxyStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
	}
	
	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		engine.runAction(new NetworkTimeoutAction(executor, milliseconds));
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return (Integer) engine.getPropertyValue(ProxyConstants.NETWORK_TIMEOUT_ACTION);
	}
	
	@Override
	public boolean isValid(int timeout) throws SQLException {
		int proxyTimeout = (int) engine.getPropertyValue(ProxyConstants.NETWORK_TIMEOUT_ACTION);
		
		return timeout <= proxyTimeout;
	}
	
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		engine.runAction(new AutoCommitAction(autoCommit));
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return (Boolean) engine.getPropertyValue(ProxyConstants.AUTO_COMMIT_ACTION);
	}

	@Override
	public void commit() throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		//Map<ConnectionUnit, Savepoint> save = new HashMap<>();
		ConnectionUnit u = null;
		
		log.log(Level.INFO, "Commiting changes.");
		
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				//save.put(u, c.setSavepoint());
				
				log.log(Level.FINE, "Commiting connection: " + u.getName());
				c.commit();
			}
			
		} catch (SQLException e) {
			//String rollBack = returnChanges(save);
			String exc = "Unable to commit connection " + u.getName() + ". Original message: " + e.getMessage();
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		//releaseSavepoint(save);
	}
	
	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		engine.runAction(new ReadOnlyAction(readOnly));
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return (Boolean) engine.getPropertyValue(ProxyConstants.READ_ONLY_ACTION);
	}
	
	@Override
	public Savepoint setSavepoint() throws SQLException {
		return setSavepoint(null);
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, Savepoint> save = new HashMap<>();
		ConnectionUnit u = null;
		
		log.log(Level.INFO, "Setting savepoint with name = " + name);
		
		
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				log.log(Level.FINE, "Setting savepoint to connection " + u.getName());
				save.put(u, c.setSavepoint());
			}
			
		} catch (SQLException e) {
			//String rollBack = returnChanges(save);
			String rollBack = new String();
			
			log.log(Level.SEVERE, "Setting savepoint failed in connection " + u.getName() + ". Releasing savepoints..");
			
			try {
				releaseSavepoint(save);
			} catch (SQLException sqle) {
				rollBack = sqle.getMessage();
			}
			
			currTransaction = null;
			
			String exc = "Unable to set savepoint " + u.getName() + ". Original message: " + e.getMessage() + rollBack;
			
			
			log.log(Level.SEVERE, "Whole message: " + exc);
			throw new SQLException(exc);
		}
		
		ProxySavepoint res = new ProxySavepoint(name, save);
		currTransaction = res;
		
		return res;
	}
	
	@Override
	public void rollback() throws SQLException {
		log.log(Level.INFO, "Doing rollback.");
		
		if (currTransaction == null) {
			String exc = "No available savepoints!";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		rollback(currTransaction);
		
	}
	
	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		ProxySavepoint ps;
		
		log.log(Level.INFO, "Doing rollback with savepoint, ID = " + savepoint.getSavepointId() + ", name = " + savepoint.getSavepointName());
		
		try {
			ps = (ProxySavepoint) savepoint;
		} catch (ClassCastException e) {
			String exc = "Invalid savepoint:  ID = " + savepoint.getSavepointId() + ", name = " + savepoint.getSavepointName();
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		Map<ConnectionUnit, Savepoint> saveList = ps.getSavepoints();
		//Map<ConnectionUnit, Savepoint> saved = new HashMap<>();
		ConnectionUnit u = null;
		
		try {
			for (Entry<ConnectionUnit, Savepoint> entry : saveList.entrySet()) {
				u = entry.getKey();
				Connection c = u.getConnection();
				Savepoint s = entry.getValue();
				
				log.log(Level.FINE, "Doing rollback in connection " + u.getName());
				c.rollback(s);
			}
		} catch (SQLException e) {
			//String rollBack = returnChanges(saved);
			String exc = "Unable rollback connection " + u.getName() + ". Original message: " + e.getMessage();
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		//releaseSavepoint(saved);
		
	}
	
	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		ProxySavepoint ps;
		
		log.log(Level.INFO, "Releasing savepoint, ID = " + savepoint.getSavepointId() + ", name = " + savepoint.getSavepointName());
		
		try {
			ps = (ProxySavepoint) savepoint;
		} catch (ClassCastException e) {
			String exc = "Invalid savepoint:  ID = " + savepoint.getSavepointId() + ", name = " + savepoint.getSavepointName();
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		Map<ConnectionUnit, Savepoint> saveList = ps.getSavepoints();
		ConnectionUnit u = null;
		String errMessage = new String();
		boolean first = true;
		
		for (Entry<ConnectionUnit, Savepoint> entry : saveList.entrySet()) {
			u = entry.getKey();
			Connection c = u.getConnection();
			Savepoint s = entry.getValue();
			
			log.log(Level.FINE, "Releasing savepoint in connection " + u.getName());
			try {
				c.releaseSavepoint(s);
			} catch (SQLException e) {
				
				if (first) {
					first = false;
				} else {
					errMessage += '\n';
				}
				
				String exc = "Cannot release savepoint in connection " + u.getName() + ". Original message: " + e.getMessage();
				
				log.log(Level.SEVERE, exc);
				errMessage += exc;
			}
			
			
		}
		
		if (! errMessage.isEmpty()) {
			log.log(Level.SEVERE, "Whole message: " + errMessage);
			throw new SQLException(errMessage);
		}
	}
	
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		log.log(Level.FINE, "Getting database info from default connection");
		
		Connection c = switcher.getDefaultConnection().getConnection();
		
		return c.getMetaData();
	}
	
	@Override
	public void setCatalog(String catalog) throws SQLException {
		engine.runAction(new CatalogAction(catalog));
	}

	@Override
	public String getCatalog() throws SQLException {
		return (String) engine.getPropertyValue(ProxyConstants.CATALOG_ACTION);
	}
	
	@Override
	public void setSchema(String schema) throws SQLException {
		engine.runAction(new SchemaAction(schema));
	}

	@Override
	public String getSchema() throws SQLException {
		return (String) engine.getPropertyValue(ProxyConstants.SCHEMA_ACTION);
	}
	
	@Override
	public void clearWarnings() throws SQLException {
		engine.runSimpleAction(new ClearWarningsAction());
	}
	
	@Override
	public SQLWarning getWarnings() throws SQLException {
		ISimpleAction action = new GetWarningsAction();
		engine.runSimpleAction(action);
		
		return (SQLWarning) action.getResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		boolean initiated = engine.isPropertyInitiated(ProxyConstants.TYPE_MAP_ACTION);
		
		if (!initiated) {
			return Collections.emptyMap();
		}
		
		return (Map<String, Class<?>>) engine.getPropertyValue(ProxyConstants.TYPE_MAP_ACTION);
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		engine.runAction(new TypeMapAction(map));
	}
	
	@Override
	public void abort(Executor executor) throws SQLException {
		engine.runSimpleAction(new AbortAction(executor));
	}
	
	@Override
	public String getClientInfo(String name) throws SQLException {
		String res = switcher.getProperties().getProperty(name);
		return res;
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return new Properties(switcher.getProperties());
	}
	
	//Unsupported
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method unwrap)");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method isWrapperFor)");
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method setTransactionIsolation)");
		
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method getTransactionIsolation)");
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method setHoldability)");
	}

	@Override
	public int getHoldability() throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method getHoldability)");
	}


	@Override
	public Clob createClob() throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method createClob)");
	}

	@Override
	public Blob createBlob() throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method createBlob)");
	}

	@Override
	public NClob createNClob() throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method createNClob)");
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method createSQLXML)");
	}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method setClientInfo)");
	}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method setClientInfo)");
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method createArrayOf)");
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		log.log(Level.SEVERE, "Unsupported Operation.");
		throw new UnsupportedOperationException("Not implemented yet. (Method createStruct)");
	}
	

}
