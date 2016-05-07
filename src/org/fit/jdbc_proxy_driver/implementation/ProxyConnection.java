package org.fit.jdbc_proxy_driver.implementation;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 
 * @author Ondřej Marek
 * 
 * This class is the implementation of the Driver. It's implemented by Connection interface
 *
 */
public class ProxyConnection implements Connection {
	private Switcher switcher;
	
	private int timeout = 0;
	private boolean timeoutSet = false;
	
	private boolean autoCommit;
	private boolean autoCommitSet = false;
	
	private boolean readOnly;
	private boolean readOnlySet;
	
	private ProxySavepoint currTransaction;
	
	private String schema;
	private boolean schemaSet = false;
	
	private String catalog;
	private boolean catalogSet = false;
	
	
	public ProxyConnection(Switcher s) throws SQLException {
		switcher = s;
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
	public List<Connection> getFailedConnections() {
		return switcher.getFailedConnections();
	}
	
	/**
	 * This method is called if some method fails during transaction and all changes has to be unmade
	 * 
	 * @param map of savepoints
	 * @return error message of unchanged connections
	 */
	private String returnChanges(Map<ConnectionUnit, Savepoint> save) {
		String res = new String();
		
		for (Entry<ConnectionUnit, Savepoint> entry : save.entrySet()) {
			ConnectionUnit rollUnit = entry.getKey();
			Connection c = rollUnit.getConnection();
			Savepoint s = entry.getValue();
			
			try {	
				c.rollback(s);
				c.releaseSavepoint(s);
			} catch (SQLException e2) {
				res += "\nUnable to return changes to former value in connection " + rollUnit.getName() + ". Original message: " + e2.getMessage();
			}
		}
		
		return res;
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
		
		for (Entry<ConnectionUnit, Savepoint> entry : save.entrySet()) {
			ConnectionUnit u = entry.getKey();
			Savepoint s = entry.getValue();
			
			try {
				u.getConnection().releaseSavepoint(s);
			} catch (SQLException e) {
				if (first) {
					first = false;
				} else {
					exc += '\n';
				}
				
				exc += "Unable to release savepoint to connection " + u.getName() + ". Original message: " + e.getMessage();
			}
			
		}
		
		if (! exc.isEmpty()) {
			throw new SQLException(exc);
		}
	}
	
	//Override methods
	
	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		Connection c = switcher.getConnection(sql);
		return c.prepareCall(sql);
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		Connection c = switcher.getConnection(sql);
		return c.prepareCall(sql, resultSetType, resultSetConcurrency);
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		Connection c = switcher.getConnection(sql);
		return c.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		Connection c = switcher.getConnection(sql);
		return c.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
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
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, Integer> save = new HashMap<>();
		ConnectionUnit u = null;
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				save.put(u, c.getNetworkTimeout());
				c.setNetworkTimeout(executor, milliseconds);
			}
			
		} catch (SQLException e) {
			String exc = new String();
			
			for (Entry<ConnectionUnit, Integer> entry : save.entrySet()) {
				ConnectionUnit cu = entry.getKey();
				Integer time = entry.getValue();
				
				try {
					cu.getConnection().setNetworkTimeout(executor, time);
				} catch (SQLException sqle) {
					timeoutSet = false;
					exc += "\nUnable to set back network timeout in connection " + cu.getName() + " to value: " + time;
				}
			}
			
			throw new SQLException("Unable to change network timeout in connection " + u.getName() + ". Original message: " + e.getMessage() + exc);
		}
		
		timeoutSet = true;
		timeout = milliseconds;
		
		
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		if (!timeoutSet) {
			throw new SQLException("Timeout has not been set yet!");
		}
		
		return timeout;
	}
	
	@Override
	public boolean isValid(int timeout) throws SQLException {
		if (!timeoutSet) {
			throw new SQLException("Timeout has not been set yet!");
		}
		
		return timeout <= this.timeout;
	}
	
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, Boolean> save = new HashMap<>();
		ConnectionUnit u = null;
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				save.put(u, c.getAutoCommit());
				c.setAutoCommit(autoCommit);
			}
			
		} catch (SQLException e) {
			String exc = new String();
			
			for (Entry<ConnectionUnit, Boolean> entry : save.entrySet()) {
				ConnectionUnit cu = entry.getKey();
				Boolean commit = entry.getValue();
				
				try {
					cu.getConnection().setAutoCommit(commit);
				} catch (SQLException sqle) {
					autoCommitSet = false;
					exc += "\nUnable to set back auto commit in connection " + cu.getName() + " to value: " + commit;
				}
			}
			
			throw new SQLException("Unable to change auto commit mode in connection " + u.getName() + ". Original message: " + e.getMessage() + exc);
		}
		
		autoCommitSet = true;
		this.autoCommit = autoCommit;
		
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		if (!autoCommitSet) {
			throw new SQLException("Auto commit has not been set yet!");
		}
		
		return autoCommit;
	}

	@Override
	public void commit() throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, Savepoint> save = new HashMap<>();
		ConnectionUnit u = null;
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				save.put(u, c.setSavepoint());
				c.commit();
			}
			
		} catch (SQLException e) {
			String rollBack = returnChanges(save);
			
			throw new SQLException("Unable to commit connection " + u.getName() + ". Original message: " + e.getMessage() + rollBack);
		}
		
		releaseSavepoint(save);
	}
	
	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, Boolean> save = new HashMap<>();
		ConnectionUnit u = null;
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				save.put(u, c.isReadOnly());
				
				c.setReadOnly(readOnly);
			}
			
		} catch (SQLException e) {
			String exc = new String();
			
			for (Entry<ConnectionUnit, Boolean> entry : save.entrySet()) {
				ConnectionUnit cu = entry.getKey();
				Boolean ro = entry.getValue();
				
				try {
					cu.getConnection().setReadOnly(ro);
				} catch (SQLException sqle) {
					readOnlySet = false;
					
					exc += "\nUnable to set back read only in connection " + cu.getName() + " to value: " + ro;
				}
			}
			
			throw new SQLException("Unable to set read only connection " + u.getName() + ". Original message: " + e.getMessage() + exc);
		}
		
		readOnlySet = true;
		this.readOnly = readOnly;
		
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		if (!readOnlySet) {
			throw new SQLException("The value read only has not been set yet!");
		}
		
		return readOnly;
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
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				save.put(u, c.setSavepoint());
			}
			
		} catch (SQLException e) {
			String rollBack = returnChanges(save);
			
			throw new SQLException("Unable to set read only connection " + u.getName() + ". Original message: " + e.getMessage() + rollBack);
		}
		
		ProxySavepoint res = new ProxySavepoint(name, save);
		currTransaction = res;
		
		return res;
	}
	
	@Override
	public void rollback() throws SQLException {
		if (currTransaction == null) {
			throw new SQLException("No available savepoints!");
		}
		
		rollback(currTransaction);
		
	}
	
	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		ProxySavepoint ps;
		
		try {
			ps = (ProxySavepoint) savepoint;
		} catch (ClassCastException e) {
			throw new SQLException("Invalid savepoint:  ID = " + savepoint.getSavepointId() + ", name = " + savepoint.getSavepointName());
		}
		
		Map<ConnectionUnit, Savepoint> saveList = ps.getSavepoints();
		Map<ConnectionUnit, Savepoint> saved = new HashMap<>();
		ConnectionUnit u = null;
		
		try {
			for (Entry<ConnectionUnit, Savepoint> entry : saveList.entrySet()) {
				u = entry.getKey();
				Connection c = u.getConnection();
				Savepoint s = entry.getValue();
				
				saved.put(u, c.setSavepoint());
				
				c.rollback(s);
			}
		} catch (SQLException e) {
			String rollBack = returnChanges(saved);
			
			throw new SQLException("Unable rollback connection " + u.getName() + ". Original message: " + e.getMessage() + rollBack);
		}
		
		releaseSavepoint(saved);
		
	}
	
	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		ProxySavepoint ps;
		
		try {
			ps = (ProxySavepoint) savepoint;
		} catch (ClassCastException e) {
			throw new SQLException("Invalid savepoint:  ID = " + savepoint.getSavepointId() + ", name = " + savepoint.getSavepointName());
		}
		
		Map<ConnectionUnit, Savepoint> saveList = ps.getSavepoints();
		ConnectionUnit u = null;
		String errMessage = new String();
		boolean first = true;
		
		for (Entry<ConnectionUnit, Savepoint> entry : saveList.entrySet()) {
			u = entry.getKey();
			Connection c = u.getConnection();
			Savepoint s = entry.getValue();
			
			try {
				c.releaseSavepoint(s);
			} catch (SQLException e) {
				if (first) {
					first = false;
				} else {
					errMessage += '\n';
				}
				
				errMessage += "Cannot release savepoint in connection " + u.getName() + ". Original message: " + e.getMessage();
			}
			
			
		}
		
		if (! errMessage.isEmpty()) {
			throw new SQLException(errMessage);
		}
	}
	
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		Connection c = switcher.getDefaultConnection().getConnection();
		
		return c.getMetaData();
	}
	
	@Override
	public void setCatalog(String catalog) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, String> save = new HashMap<>();
		ConnectionUnit u = null;
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				save.put(u, c.getCatalog());
				c.setCatalog(catalog);
			}
			
		} catch (SQLException e) {
			String exc = new String();
			
			for (Entry<ConnectionUnit, String> entry : save.entrySet()) {
				ConnectionUnit cu = entry.getKey();
				String cat = entry.getValue();
				
				try {
					cu.getConnection().setCatalog(cat);
				} catch (SQLException sqle) {
					catalogSet = false;
					
					exc += "\nUnable to set back catalog in connection " + cu.getName() + " to value: " + cat;
				}
			}
			
			throw new SQLException("Unable to change catalog in connection " + u.getName() + ". Original message: " + e.getMessage() + exc);
		}
		
		
		catalogSet = true;
		this.catalog = catalog;
	}

	@Override
	public String getCatalog() throws SQLException {
		if (!catalogSet) {
			throw new SQLException("Database catalog has not been set yet!");
		}
		
		return catalog;
	}
	
	@Override
	public void setSchema(String schema) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, String> save = new HashMap<>();
		ConnectionUnit u = null;
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				save.put(u, c.getSchema());
				c.setSchema(schema);
			}
			
		} catch (SQLException e) {
			String exc = new String();
			
			for (Entry<ConnectionUnit, String> entry : save.entrySet()) {
				ConnectionUnit cu = entry.getKey();
				String sch = entry.getValue();
				
				try {
					cu.getConnection().setSchema(sch);
				} catch (SQLException sqle) {
					schemaSet = false;
					
					exc += "\nUnable to set back schema in connection " + cu.getName() + " to value: " + sch;
				}
			}
			
			throw new SQLException("Unable to change schema in connection " + u.getName() + ". Original message: " + e.getMessage() + exc);
		}
		
		
		schemaSet = true;
		this.schema = schema;
	}

	@Override
	public String getSchema() throws SQLException {
		if (!schemaSet) {
			throw new SQLException("Database schema has not been set yet!");
		}
		
		return schema;
	}
	
	//Unsupported
	@Override
	public void abort(Executor executor) throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
		
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public int getHoldability() throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}


	@Override
	public Clob createClob() throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Blob createBlob() throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public NClob createNClob() throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}
	

}
