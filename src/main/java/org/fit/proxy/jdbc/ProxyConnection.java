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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fit.proxy.jdbc.actions.ReadOnlyAction;
import org.fit.proxy.jdbc.actions.back.ReadOnlyTakeBackAction;
import org.fit.proxy.jdbc.exception.ProxyEceptionUtils;
import org.fit.proxy.jdbc.exception.ProxyException;

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
	
	private Map<String, Class<?>> typeMap = null;
	
	
	public ProxyConnection(Switcher s) throws SQLException {
		switcher = s;
		
		setAutoCommit(true);
		setReadOnly(false);
		
		log.log(Level.INFO, "Proxy connection established.");
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
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, Integer> save = new HashMap<>();
		ConnectionUnit u = null;
		
		log.log(Level.INFO, "Setting network timeout, miliseconds = " + milliseconds);
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				log.log(Level.FINE, "Saving network timeout in connection " + u.getName());
				save.put(u, c.getNetworkTimeout());
				
				log.log(Level.FINE, "Setting network timeout in connection " + u.getName());
				c.setNetworkTimeout(executor, milliseconds);
			}
			
		} catch (SQLException e) {
			String exc = new String();
			
			log.log(Level.SEVERE, "An error occured when setting network timeout in connection " + u.getName() + ". Setting values back in other connections.");
			
			for (Entry<ConnectionUnit, Integer> entry : save.entrySet()) {
				ConnectionUnit cu = entry.getKey();
				Integer time = entry.getValue();
				
				try {
					cu.getConnection().setNetworkTimeout(executor, time);
				} catch (SQLException sqle) {
					timeoutSet = false;
					
					String message = "Unable to set back network timeout in connection " + cu.getName() + " to value: " + time;
					
					log.log(Level.SEVERE, message);
					exc += "\n" + message;
				}
			}
			
			String message = "Unable to change network timeout in connection " + u.getName() + ". Original message: " + e.getMessage() + exc;
			
			log.log(Level.SEVERE, "Whole message: " + message);
			throw new SQLException(message);
		}
		
		log.log(Level.INFO, "Network timeout was set successfully, miliseconds = " + milliseconds);
		
		timeoutSet = true;
		timeout = milliseconds;
		
		
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		log.log(Level.FINE, "Getting network timeout.");
		
		if (!timeoutSet) {
			String exc = "Timeout has not been set yet!";
			
			log.log(Level.SEVERE, exc);
			
			throw new SQLException(exc);
		}
		
		return timeout;
	}
	
	@Override
	public boolean isValid(int timeout) throws SQLException {
		log.log(Level.FINE, "Checking whether timeout " + timeout + " milliseconds is valid");
		
		if (!timeoutSet) {
			String exc = "Timeout has not been set yet!";
			
			log.log(Level.FINE, exc);
			throw new SQLException(exc);
		}
		
		return timeout <= this.timeout;
	}
	
	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, Boolean> save = new HashMap<>();
		ConnectionUnit u = null;
		
		log.log(Level.INFO, "Setting auto commit to value = " + autoCommit);
		
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				log.log(Level.FINE, "Saving auto commit in connection " + u.getName());
				save.put(u, c.getAutoCommit());
				
				log.log(Level.FINE, "Saving auto commit in connection " + u.getName());
				c.setAutoCommit(autoCommit);
			}
			
		} catch (SQLException e) {
			String exc = new String();
			
			log.log(Level.SEVERE, "Error occured when setting auto commit in connection " + u.getName() + ". Setting auto commit values back.");
			
			for (Entry<ConnectionUnit, Boolean> entry : save.entrySet()) {
				ConnectionUnit cu = entry.getKey();
				Boolean commit = entry.getValue();
				
				try {
					cu.getConnection().setAutoCommit(commit);
				} catch (SQLException sqle) {
					autoCommitSet = false;
					
					String message = "Unable to set back auto commit in connection " + cu.getName() + " to value: " + commit;
					
					log.log(Level.SEVERE, message);
					exc += "\n" + message;
				}
			}
			
			
			String message = "Unable to change auto commit mode in connection " + u.getName() + ". Original message: " + e.getMessage() + exc;
			
			log.log(Level.SEVERE, "Whole message: " + message);
			throw new SQLException(message);
		}
		
		if (!autoCommit) {
			log.log(Level.FINE, "Setting savepoint.");
			currTransaction = (ProxySavepoint) setSavepoint();
		}
		
		log.log(Level.INFO, "Setting auto commit to value " + autoCommit + " was successful.");
		
		autoCommitSet = true;
		this.autoCommit = autoCommit;
		
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		log.log(Level.FINE, "Getting auto commit");
		
		if (!autoCommitSet) {
			String exc = "Auto commit has not been set yet!";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		return autoCommit;
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
		Map<ConnectionUnit, Boolean> save = new HashMap<>();
		
		try {
			new ReadOnlyAction(switcher, readOnly, save).run();
			this.readOnly = readOnly;
			this.readOnlySet = true;
			
			log.fine(new StringBuilder("Proxy connection set read only = ").append(readOnly).toString());
		} catch (SQLException e) {
			try {
				new ReadOnlyTakeBackAction(save).run();
				log.log(Level.WARNING, new StringBuilder("Cannot set proxy connection to read only = ").append(readOnly).toString(), e);
			} catch (ProxyException pe) {
				pe.setCause(e);
				ProxyEceptionUtils.throwAndLogAsSql(pe, Level.SEVERE);
			}
		}		
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		log.log(Level.FINE, "Checking whether connection is read only.");
		
		if (!readOnlySet) {
			String exc = "The value read only has not been set yet!";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
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
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, String> save = new HashMap<>();
		ConnectionUnit u = null;
		
		log.log(Level.INFO, "Setting catalog to value = " + catalog);
		
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				log.log(Level.FINE, "Saving catalog in connection " + u.getName());
				save.put(u, c.getCatalog());
				
				log.log(Level.FINE, "Setting catalog in connection " + u.getName());
				c.setCatalog(catalog);
			}
			
		} catch (SQLException e) {
			String exc = new String();
			
			log.log(Level.SEVERE, "Setting catalog in connection " + u.getName() + " failed. Setting catalog back.");
			
			for (Entry<ConnectionUnit, String> entry : save.entrySet()) {
				ConnectionUnit cu = entry.getKey();
				String cat = entry.getValue();
				
				try {
					cu.getConnection().setCatalog(cat);
				} catch (SQLException sqle) {
					catalogSet = false;
					
					String message = "Unable to set back catalog in connection " + cu.getName() + " to value: " + cat;
					
					log.log(Level.SEVERE, message);
					exc += "\n" + message;
				}
			}
			
			String message = "Unable to change catalog in connection " + u.getName() + ". Original message: " + e.getMessage() + exc;
			
			log.log(Level.SEVERE, "Whole message: " + message);
			throw new SQLException(message);
		}
		
		
		catalogSet = true;
		this.catalog = catalog;
	}

	@Override
	public String getCatalog() throws SQLException {
		log.log(Level.FINE, "Getting catalog..");
		
		if (!catalogSet) {
			String exc = "Database catalog has not been set yet!";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		return catalog;
	}
	
	@Override
	public void setSchema(String schema) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, String> save = new HashMap<>();
		ConnectionUnit u = null;
		
		log.log(Level.INFO, "Setting schema to value = " + schema);
		
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				log.log(Level.FINE, "Saving schema in connection " + u.getName());
				save.put(u, c.getSchema());
				
				log.log(Level.FINE, "Setting schema in connection " + u.getName());
				c.setSchema(schema);
			}
			
		} catch (SQLException e) {
			String exc = new String();
			log.log(Level.SEVERE, "Setting schema failed in connection " + u.getName());
			
			for (Entry<ConnectionUnit, String> entry : save.entrySet()) {
				ConnectionUnit cu = entry.getKey();
				String sch = entry.getValue();
				
				try {
					cu.getConnection().setSchema(sch);
				} catch (SQLException sqle) {
					schemaSet = false;
					
					String message = "Unable to set back schema in connection " + cu.getName() + " to value: " + sch;
					exc += "\n" + message;
				}
			}
			
			String message = "Unable to change schema in connection " + u.getName() + ". Original message: " + e.getMessage() + exc;
			
			log.log(Level.SEVERE, "Whole message: " + message);
			throw new SQLException(message);
		}
		
		
		schemaSet = true;
		this.schema = schema;
	}

	@Override
	public String getSchema() throws SQLException {
		log.log(Level.FINE, "Getting schema");
		
		if (!schemaSet) {
			String exc = "Database schema has not been set yet!";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		return schema;
	}
	
	@Override
	public void clearWarnings() throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		String exc = new String();
		
		log.log(Level.INFO, "Clearing warnings");
		
		for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
			ConnectionUnit cu = it.next();
			
			log.log(Level.FINE, "Clearing warnings in connection " + cu.getName());
			try {
				cu.getConnection().clearWarnings();
			} catch (SQLException e) {
				if (! exc.isEmpty()) {
					exc += '\n';
				}
				
				String message = "Unable to clear warnings in connection: " + cu.getName() + ". Original message: " + e.getMessage();
				
				log.log(Level.SEVERE, message);
				exc += message;
			}
		}
		
		if (! exc.isEmpty()) {
			log.log(Level.SEVERE, "Whole message:" + exc);
			throw new SQLException(exc);
		}
		
		log.log(Level.INFO, "Warnings cleared successfully.");
	}
	
	@Override
	public SQLWarning getWarnings() throws SQLException {
		SQLWarning res = null;
		
		ConnectionUnit conn = null;
		
		log.log(Level.FINE, "Getting warnings from default connetion");
		
		try {
			conn = switcher.getDefaultConnection();
			res = conn.getConnection().getWarnings();
		} catch (SQLException e) {
			log.log(Level.FINE, "Default connection does not exists");
		}
		
		for (Iterator<ConnectionUnit> it = switcher.getConnectionList().iterator(); res != null && it.hasNext();) {
			conn = it.next();
			
			log.log(Level.FINE, "Getting warnings from connection " + conn.getName());
			
			res = conn.getConnection().getWarnings();
		}
		
		return res;
	}
	
	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		log.log(Level.FINE, "Getting type map");
		
		if (typeMap == null) {
			String exc = "Typemap is not set!";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		return typeMap;
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		Map<ConnectionUnit, Map<String, Class<?>>> save = new HashMap<>();
		ConnectionUnit u = null;
		
		log.log(Level.INFO, "Setting type map");
		try {
			for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
				u = it.next();
				Connection c = u.getConnection();
				
				log.log(Level.FINE, "Saving type map in connection " + u.getName());
				save.put(u, c.getTypeMap());
				
				log.log(Level.FINE, "Setting type map in connection " + u.getName());
				c.setTypeMap(map);
			}
			
		} catch (SQLException e) {
			String exc = new String();
			
			log.log(Level.SEVERE, "Setting type map failed in connection " + u.getName() + ". Setting type mapas back.");
			
			for (Entry<ConnectionUnit, Map<String, Class<?>>> entry : save.entrySet()) {
				ConnectionUnit cu = entry.getKey();
				Map<String, Class<?>> typeMap = entry.getValue();
				
				try {
					cu.getConnection().setTypeMap(typeMap);
				} catch (SQLException sqle) {
					schemaSet = false;
					
					String message = "Unable to set back type map in connection " + cu.getName();
					
					log.log(Level.SEVERE, message);
					exc += "\n" + message;
				}
			}
			
			String message = "Unable to change type map in connection " + u.getName() + ". Original message: " + e.getMessage() + exc;
			
			log.log(Level.SEVERE, "Whole message: " + message);
			throw new SQLException(message);
		}
		
		log.log(Level.INFO, "Type map was changed successfully.");
		
		typeMap = map;
	}
	
	@Override
	public void abort(Executor executor) throws SQLException {
		List<ConnectionUnit> l = switcher.getConnectionList();
		String exc = new String();
		
		log.log(Level.INFO, "Aborting...");
		
		for (Iterator<ConnectionUnit> it = l.iterator(); it.hasNext();) {
			ConnectionUnit cu = it.next();
			
			log.log(Level.FINE, "Aborting connection " + cu.getName());
			
			try {
				cu.getConnection().abort(executor);
			} catch (SQLException e) {
				if (! exc.isEmpty()) {
					exc += '\n';
				}
				
				String message = "Unable to abort connection: " + cu.getName() + ". Original message: " + e.getMessage();
				
				log.log(Level.SEVERE, message);
				exc += message;
			}
		}
		
		if (! exc.isEmpty()) {
			log.log(Level.SEVERE, "Whole message: " + exc);
			throw new SQLException(exc);
		}
	}
	
	@Override
	public String getClientInfo(String name) throws SQLException {
		log.log(Level.FINE, "Getting proxy property named " + name);
		
		String res = switcher.getProperties().getProperty(name);
		
		if (res == null) {
			String exc = "Property " + name + "is not contained.";
			
			log.log(Level.WARNING, exc);
			throw new SQLException(exc);
		}
		
		return res;
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		log.log(Level.FINE, "Getting proxy properties");
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
