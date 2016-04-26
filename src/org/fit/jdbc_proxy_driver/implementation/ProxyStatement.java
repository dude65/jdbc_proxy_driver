package org.fit.jdbc_proxy_driver.implementation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

/**
 * Object ProxyStatement is used when someone calls createStatement() in class Driver. All results depends on last called executeXXX method.
 * 
 * @author Ondřej Marek
 *
 */
public class ProxyStatement implements Statement {
	private Switcher switcher;
	private ConnectionUnit current = null;
	private Statement statement = null;
	private int[] statementVal;
	
	public ProxyStatement(Switcher s) {
		statementVal = new int[0];
		switcher = s;
	}
	
	public ProxyStatement(Switcher s, int resultSetType, int resultSetConcurrency) {
		statementVal = new int [2];
		statementVal[0] = resultSetType;
		statementVal[1] = resultSetConcurrency;
		
		switcher = s;
	}
	
	public ProxyStatement(Switcher s, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
		statementVal = new int [3];
		statementVal[0] = resultSetType;
		statementVal[1] = resultSetConcurrency;
		statementVal[2] = resultSetHoldability;
		
		switcher = s;
	}
	
	private void setCurrentStatement(String sql) throws SQLException {
		Connection c = switcher.getConnection(sql);
		
		if (c != current.getConnection()) {
			switch (statementVal.length) {
			case 2: statement = c.createStatement(statementVal[0], statementVal[1]); break;
			case 3: statement = c.createStatement(statementVal[0], statementVal[1], statementVal[2]); break;
			default: statement = c.createStatement();
			}
		}
	}
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return statement.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return statement.isWrapperFor(iface);
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		setCurrentStatement(sql);
		return statement.executeQuery(sql);
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		setCurrentStatement(sql);
		return statement.executeUpdate(sql);
	}

	@Override
	public void close() throws SQLException {
		statement.close();
		
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		return statement.getMaxFieldSize();
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		statement.setMaxFieldSize(max);
		
	}

	@Override
	public int getMaxRows() throws SQLException {
		return statement.getMaxRows();
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		statement.setMaxRows(max);
		
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		statement.setEscapeProcessing(enable);
		
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		return statement.getQueryTimeout();
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		statement.setQueryTimeout(seconds);
		
	}

	@Override
	public void cancel() throws SQLException {
		statement.cancel();
		
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return statement.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		statement.clearWarnings();
		
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		statement.setCursorName(name);
		
	}

	@Override
	public boolean execute(String sql) throws SQLException {
		setCurrentStatement(sql);
		return statement.execute(sql);
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		return statement.getResultSet();
	}

	@Override
	public int getUpdateCount() throws SQLException {
		return statement.getUpdateCount();
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		return statement.getMoreResults();
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		statement.setFetchDirection(direction);
		
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return statement.getFetchDirection();
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		statement.setFetchSize(rows);
		
	}

	@Override
	public int getFetchSize() throws SQLException {
		return statement.getFetchSize();
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		return statement.getResultSetConcurrency();
	}

	@Override
	public int getResultSetType() throws SQLException {
		return statement.getResultSetType();
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		setCurrentStatement(sql);
		
		statement.addBatch(sql);
		
	}

	@Override
	public void clearBatch() throws SQLException {
		statement.clearBatch();
		
	}

	@Override
	public int[] executeBatch() throws SQLException {
		return statement.executeBatch();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return statement.getConnection();
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		return statement.getMoreResults(current);
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		return statement.getGeneratedKeys();
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		setCurrentStatement(sql);
		
		return statement.executeUpdate(sql, autoGeneratedKeys);
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		setCurrentStatement(sql);
		
		return statement.executeUpdate(sql, columnIndexes);
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		setCurrentStatement(sql);
		
		return statement.executeUpdate(sql, columnNames);
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		setCurrentStatement(sql);
		
		return statement.execute(sql, autoGeneratedKeys);
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		setCurrentStatement(sql);
		
		return statement.execute(sql, columnIndexes);
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		setCurrentStatement(sql);
		
		return statement.execute(sql, columnNames);
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return statement.getResultSetHoldability();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return statement.isClosed();
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		statement.setPoolable(poolable);
		
	}

	@Override
	public boolean isPoolable() throws SQLException {
		return statement.isPoolable();
	}

	@Override
	public void closeOnCompletion() throws SQLException {
		statement.closeOnCompletion();
		
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		return statement.isCloseOnCompletion();
	}

}