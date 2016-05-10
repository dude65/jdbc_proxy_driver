package org.fit.jdbc_proxy_driver.implementation;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author Ondřej Marek
 * 
 * Object ProxyStatement is used when someone calls createStatement() in class Driver. All results depends on last called executeXXX method.
 *
 */
public class ProxyStatement implements Statement {
	private ProxyConnection connection;

	private Statement statement = null;
	
	private Queue<SQLWarning> warnings = new LinkedList<>();
	
	private int resultSetType = 0;
	private int resultSetConcurrency = 0;
	private int resultSetHoldability = 0;
	
	private int maxFieldVal;
	private boolean maxFieldSet = false;
	
	private int maxRowVal;
	private boolean maxRowSet = false;
	
	private boolean escapeProcessingVal;
	private boolean escapeProcessingSet;
	
	private int qTimeOutVal;
	private boolean qTimeOutSet = false;
	
	private String cursorVal;
	private boolean cursorSet = false;
	
	private int fetchDirVal;
	private boolean fetchDirSet = false;
	
	private int fetchSizeVal;
	private boolean fetchSizeSet = false;
	
	private boolean poolableVal;
	private boolean poolableSet = false;
	
	List<CallableStatement> batchList = new LinkedList<>();
	
	public ProxyStatement(ProxyConnection pc) {
		connection = pc;
	}
	
	public ProxyStatement(ProxyConnection pc, int resultSetType, int resultSetConcurrency) {
		connection = pc;
		
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;
	}
	
	public ProxyStatement(ProxyConnection pc, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
		connection = pc;
		
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;
		this.resultSetHoldability = resultSetHoldability;
	}
	
	private void setCurrentStatement(String sql) throws SQLException {
		
		
		if (statement != null) {
			if (statement.isClosed()) {
				throw new SQLException("The statement is closed!");
			}
			
			SQLWarning w;
			
			while ((w = statement.getWarnings()) != null) {
				warnings.add(w);
			}
			
			statement.close();
			
			
		}
		
		statement = connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		
		if (maxFieldSet) {
			statement.setMaxFieldSize(maxFieldVal);
		}
		
		if (maxRowSet) {
			statement.setMaxRows(maxRowVal);
		}
		
		if (escapeProcessingSet) {
			statement.setEscapeProcessing(escapeProcessingVal);
		}
		
		if (qTimeOutSet) {
			statement.setQueryTimeout(qTimeOutVal);
		}
		
		if (cursorSet) {
			statement.setCursorName(cursorVal);
		}
		
		if (fetchDirSet) {
			statement.setFetchDirection(fetchDirVal);
		}
		
		if (fetchSizeSet) {
			statement.setFetchSize(fetchSizeVal);
		}
		
		if (poolableSet) {
			statement.setPoolable(poolableVal);
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
		clearBatch();
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		return statement.getMaxFieldSize();
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		statement.setMaxFieldSize(max);
		
		maxFieldVal = max;
		maxFieldSet = true;
		
	}

	@Override
	public int getMaxRows() throws SQLException {
		return statement.getMaxRows();
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		statement.setMaxRows(max);
		
		maxRowVal = max;
		maxRowSet = false;
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		statement.setEscapeProcessing(enable);
		
		escapeProcessingVal = enable;
		escapeProcessingSet = true;
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		return statement.getQueryTimeout();
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		statement.setQueryTimeout(seconds);
		
		qTimeOutVal = seconds;
		qTimeOutSet = true;
	}

	@Override
	public void cancel() throws SQLException {
		statement.cancel();
		
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		SQLWarning res;
		
		if (warnings.isEmpty()) {
			res = statement.getWarnings();
		} else {
			res = warnings.poll();
		}
		
		return res;
	}

	@Override
	public void clearWarnings() throws SQLException {
		warnings.clear();
		statement.clearWarnings();
		
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		statement.setCursorName(name);
		
		cursorVal = name;
		cursorSet = true;
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
		
		fetchDirVal = direction;
		fetchDirSet = true;
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return statement.getFetchDirection();
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		statement.setFetchSize(rows);
		
		fetchSizeVal = rows;
		fetchSizeSet = true;
		
	}

	@Override
	public int getFetchSize() throws SQLException {
		return statement.getFetchSize();
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		return resultSetConcurrency;
	}

	@Override
	public int getResultSetType() throws SQLException {
		return statement.getResultSetType();
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		try {
			CallableStatement s = connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
			batchList.add(s);
		} catch (SQLException e) {
			throw new SQLException("Unable to add batch, orriginal message: " + e.getMessage());
		}
		
	}

	@Override
	public void clearBatch() throws SQLException {
		String exc = new String();
		boolean first = false;
		
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
				
				exc += "Unable to close statement during clearing batch. Original message: " + e.getMessage();
			}
			
			
		}
		
		if (! exc.isEmpty()) {
			throw new SQLException(exc);
		}
		
	}

	@Override
	public int[] executeBatch() throws SQLException {
		int[] res = new int[batchList.size()];
		int i = 0;
		
		try {
			for (Iterator<CallableStatement> it = batchList.iterator(); it.hasNext(); i++) {
				CallableStatement s = it.next();
				
				res[i] = s.executeUpdate();
			}
		} catch (SQLException e) {
			throw new SQLException("Executing batch failed, original message: " + e.getMessage());
		}
		
		
		return res;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return connection;
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
		return resultSetHoldability;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return statement.isClosed();
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		statement.setPoolable(poolable);
		
		poolableVal = poolable;
		poolableSet = true;
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
