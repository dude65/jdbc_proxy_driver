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
		return new ProxyStatement(switcher);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return new ProxyStatement(switcher, resultSetType, resultSetConcurrency);
	}
	
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return new ProxyStatement(switcher, resultSetType, resultSetConcurrency, resultSetHoldability);
	}
	
	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, Savepoint> save = new HashMap<>();
		ConnectionUnit u = null;
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				save.put(u, c.setSavepoint());
				c.setNetworkTimeout(executor, milliseconds);
			}
			
		} catch (SQLException e) {
			timeoutSet = false;
			String rollBack = returnChanges(save);
			
			throw new SQLException("Unable to change network timeout in connection " + u.getName() + ". Original message: " + e.getMessage() + rollBack);
		}
		
		timeoutSet = true;
		releaseSavepoint(save);

		
		
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		if (!timeoutSet) {
			throw new SQLException("Timeout has not been set yet!");
		}
		
		return timeout;
	}
	
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, Savepoint> save = new HashMap<>();
		ConnectionUnit u = null;
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				save.put(u, c.setSavepoint());
				c.setAutoCommit(autoCommit);
			}
			
		} catch (SQLException e) {
			autoCommitSet = false;
			String rollBack = returnChanges(save);
			
			throw new SQLException("Unable to change auto commit mode in connection " + u.getName() + ". Original message: " + e.getMessage() + rollBack);
		}
		
		autoCommitSet = true;
		this.autoCommit = autoCommit;
		
		releaseSavepoint(save);
		
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
	
	//TODO features
	
	
	
	
	
	
	@Override
	public void abort(Executor executor) throws SQLException {
		// TODO Auto-generated method stub
		
	}
	
	
	
	//Unsupported
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	

	

	@Override
	public void rollback() throws SQLException {
		// TODO Auto-generated method stub
		
	}
	
	



	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCatalog() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Clob createClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Blob createBlob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NClob createNClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSchema() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	

	

}
