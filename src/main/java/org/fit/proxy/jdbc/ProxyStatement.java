package org.fit.proxy.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.fit.proxy.jdbc.configuration.ProxyConstants;

/**
 * @author Ondřej Marek
 * 
 * Object ProxyStatement is used when someone calls createStatement() in class Driver. All results depends on last called executeXXX method.
 *
 */
public class ProxyStatement implements Statement {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	
	private final ProxyStatementEngine engine;
	
	
	public ProxyStatement(ProxyConnection pc) throws SQLException {
		StatementConstructorFactory factory = new StatementConstructorFactory();
		
		this.engine = new ProxyStatementEngine(pc, factory);
	}
	
	public ProxyStatement(ProxyConnection pc, int resultSetType, int resultSetConcurrency) throws SQLException {
		StatementConstructorFactory factory = new StatementConstructorFactory(resultSetType, resultSetConcurrency);
		
		this.engine = new ProxyStatementEngine(pc, factory);
	}
	
	public ProxyStatement(ProxyConnection pc, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		StatementConstructorFactory factory = new StatementConstructorFactory(resultSetType, resultSetConcurrency, resultSetHoldability);
		
		this.engine = new ProxyStatementEngine(pc, factory);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T retrieveStatementProperty(String type, boolean isBoolean) throws SQLException {
		ProxyProperiesHelper provider = engine.getPropertiesHelper();
		
		if (provider.isPropertySet(type)) {
			return (T) provider.getPropertyValue(type);
		}
		
		Statement statement = engine.getStatement();
		String prefix = (isBoolean) ? "is" : "get";
		
		try {
			String getterName = prefix + StringUtils.capitalize(type);
			Method getterMethod = statement.getClass().getMethod(getterName);
			Object res = getterMethod.invoke(engine.getStatement());
			
			return (T) res;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new SQLException("Unable to get statement property: " + type, e);
		}
		
	}
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return engine.getStatement().unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return engine.getStatement().isWrapperFor(iface);
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		log.fine("Execute query in proxy statement: " + sql);
		
		return engine.getStatement(sql).executeQuery(sql);
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {	
		log.fine("Execute update in proxy statement: " + sql);
		
		return engine.getStatement(sql).executeUpdate(sql);
	}

	@Override
	public void close() throws SQLException {
		log.fine("Closing proxy statement.");
		
		engine.close();
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		Integer fieldSize = retrieveStatementProperty(ProxyConstants.MAX_FIELD_SIZE, false);
		return fieldSize.intValue();
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		engine.getStatement().setFetchSize(max);
		engine.getPropertiesHelper().setProperty(ProxyConstants.MAX_FIELD_SIZE, max);		
	}

	@Override
	public int getMaxRows() throws SQLException {
		Integer maxRows = retrieveStatementProperty(ProxyConstants.MAX_ROWS, false);
		return maxRows.intValue();
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		engine.getStatement().setMaxRows(max);
		engine.getPropertiesHelper().setProperty(ProxyConstants.MAX_ROWS, max);
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		engine.getStatement().setEscapeProcessing(enable);
		engine.getPropertiesHelper().setProperty(ProxyConstants.ESCAPE_PROCESSING, enable);
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		Integer timeout = retrieveStatementProperty(ProxyConstants.QUERY_TIMEOUT, false);
		return timeout.intValue();
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		engine.getStatement().setQueryTimeout(seconds);
		engine.getPropertiesHelper().setProperty(ProxyConstants.QUERY_TIMEOUT, seconds);
	}

	@Override
	public void cancel() throws SQLException {
		log.info("Cancelling proxy statement.");
		
		try {
			engine.getStatement().cancel();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Unable to cancel proxy statement.", e);
			throw e;
		}
		
		log.info("Proxy statement canceled successfully.");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return engine.getStatement().getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		log.fine("Clearing warnings");
		
		engine.getStatement().clearWarnings();
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		log.fine("Setting cursor name to val = " + name);
		
		engine.getStatement().setCursorName(name);
		engine.getPropertiesHelper().setProperty(ProxyConstants.CURSOR_NAME, name);
	}

	@Override
	public boolean execute(String sql) throws SQLException {
		log.fine(new StringBuilder("Executing query: (").append(sql).append(").").toString());
		
		return engine.getStatement(sql).execute(sql);
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		return engine.getStatement().getResultSet();
	}

	@Override
	public int getUpdateCount() throws SQLException {
		return engine.getStatement().getUpdateCount();
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		return engine.getStatement().getMoreResults();
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		log.fine("Setting fetch direction = " + direction);
		
		engine.getStatement().setFetchDirection(direction);
		engine.getPropertiesHelper().setProperty(ProxyConstants.FETCH_DIR, direction);
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return retrieveStatementProperty(ProxyConstants.FETCH_DIR, false);
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		log.fine("Setting fetch size = " + rows);
		
		engine.getStatement().setFetchSize(rows);
		engine.getPropertiesHelper().setProperty(ProxyConstants.FETCH_SIZE, rows);
		
	}

	@Override
	public int getFetchSize() throws SQLException {
		return retrieveStatementProperty(ProxyConstants.FETCH_SIZE, false);
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		return engine.getStatement().getResultSetConcurrency();
	}

	@Override
	public int getResultSetType() throws SQLException {
		return engine.getStatement().getResultSetType();
	}

	//TODO
	@Override
	public void addBatch(String sql) throws SQLException {
		throw new UnsupportedOperationException("Temporarily not available");
		/*try {
			log.log(Level.INFO, "Adding batch: " + sql);
			CallableStatement s = connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
			batchList.add(s);
		} catch (SQLException e) {
			String exc = "Unable to add batch, orriginal message: " + e.getMessage();
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}*/
		
	}

	@Override
	public void clearBatch() throws SQLException {
		throw new UnsupportedOperationException("Temporarily not available");
		/*String exc = new String();
		boolean first = false;
		
		log.log(Level.INFO, "Clearing batch.");
		
		for (Iterator<CallableStatement> i = batchList.iterator(); i.hasNext();) {
			Statement s = i.next();
			
			try {
				s.close();
			} catch (SQLException e) {
				if (first) {
					first = false;
				} else {
					exc += '\n';
				}
				
				log.log(Level.SEVERE, "Error when closing batch statement.");
				exc += "Unable to close statement when clearing batch. Original message: " + e.getMessage();
			}
			
			
		}
		
		if (! exc.isEmpty()) {
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		batchList.clear();
		*/
	}

	@Override
	public int[] executeBatch() throws SQLException {
		throw new UnsupportedOperationException("Temporarily not available");
		/*int[] res = new int[batchList.size()];
		int i = 0;
		
		log.log(Level.INFO, "Executing batch.");
		try {
			for (Iterator<CallableStatement> it = batchList.iterator(); it.hasNext(); i++) {
				CallableStatement s = it.next();
				
				log.log(Level.FINE, "Executing batch statement #" + i);
				res[i] = s.executeUpdate();
			}
		} catch (SQLException e) {
			String exc = "Executing batch failed, original message: " + e.getMessage();
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		log.log(Level.INFO, "Batch executing was successful.");
		
		return res;*/
	}

	@Override
	public Connection getConnection() throws SQLException {
		return engine.getConnection();
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		return engine.getStatement().getMoreResults(current);
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		return engine.getStatement().getGeneratedKeys();
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		String key = ProxyTools.getAutoGeneratedKeysDescription(autoGeneratedKeys);
		
		StringBuilder description = new StringBuilder("Executing update with autoGeneratedKeys = ");
		description.append(key).append('.');
		
		log.fine(description.toString());
		return engine.getStatement(sql).executeUpdate(sql, autoGeneratedKeys);
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		log.fine("Executing update with columnIndexes = " + columnIndexes);
		
		return engine.getStatement(sql).executeUpdate(sql, columnIndexes);
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		log.fine("Executing update with columnNames = " + columnNames);
		
		return engine.getStatement(sql).executeUpdate(sql, columnNames);
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		String key = ProxyTools.getAutoGeneratedKeysDescription(autoGeneratedKeys);
		
		log.fine("Executing query with autoGeneratedKeys = " + key);
		return engine.getStatement(sql).execute(sql, autoGeneratedKeys);
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		log.fine("Executing query with columnIndexes = " + columnIndexes);
		
		return engine.getStatement(sql).execute(sql, columnIndexes);
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		log.fine("Executing query '" + sql + "'with columnNames = " + columnNames);
		
		Statement statement = engine.getStatement(sql);
		
		return statement.execute(sql, columnNames);
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		Integer holdability = retrieveStatementProperty(ProxyConstants.RESULT_SET_HOLDABILITY, false);
		
		return holdability;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return engine.isStatementClosed();
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		Statement statement = engine.getStatement();
		statement.setPoolable(poolable);
		
		engine.getPropertiesHelper().setProperty(ProxyConstants.POOLABLE, poolable);
	}

	@Override
	public boolean isPoolable() throws SQLException {
		Boolean poolable = retrieveStatementProperty(ProxyConstants.POOLABLE, true);
		return poolable;
	}

	@Override
	public void closeOnCompletion() throws SQLException {
		engine.getStatement().closeOnCompletion();
		
		engine.getPropertiesHelper().setProperty(ProxyConstants.CLOSE_ON_COMPLETION, Boolean.TRUE);
		
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		Boolean closeOnCompletition = retrieveStatementProperty(ProxyConstants.CLOSE_ON_COMPLETION, true);
		
		return closeOnCompletition;
	}

}
