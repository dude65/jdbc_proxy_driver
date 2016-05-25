package org.fit.proxy.jdbc;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ondřej Marek
 * 
 * Object ProxyStatement is used when someone calls createStatement() in class Driver. All results depends on last called executeXXX method.
 *
 */
public class ProxyStatement implements Statement {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	
	private ProxyConnection connection;

	private Statement statement;
	
	private Queue<SQLWarning> warnings = new LinkedList<>();
	
	private int resultSetType;
	private int resultSetConcurrency;
	private int resultSetHoldability;
	
	private final int resultSetCase;
	
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
		resultSetCase = 1;
		
		initiateStatement();
	}
	
	public ProxyStatement(ProxyConnection pc, int resultSetType, int resultSetConcurrency) {
		connection = pc;
		resultSetCase = 2;
		
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;
		initiateStatement();
	}
	
	public ProxyStatement(ProxyConnection pc, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
		connection = pc;
		resultSetCase = 3;
		
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;
		this.resultSetHoldability = resultSetHoldability;
		
		initiateStatement();
	}
	
	/**
	 * Initiates 
	 */
	private final void initiateStatement() {
		Switcher s = connection.getSwitcher();
		
		try {
			statement = s.getDefaultConnection().getConnection().createStatement();
		} catch (SQLException | NullPointerException e) {
			List<ConnectionUnit> l = s.getConnectionList();
			
			try {
				statement = l.get(0).getConnection().createStatement();
			} catch (SQLException sqle) {
				log.log(Level.SEVERE, "Could not set up en empty statement. Null pointer exception may occur when calling for getting information about statement.");
			}
		}
		
		log.log(Level.INFO, "Proxy statement has been initiated. Values: resultSetType = " + resultSetType + ", resultSetConcurrency = " + resultSetConcurrency);
	}
	
	/**
	 * This gets the right statement according to which constructor was called
	 * @param sql query
	 * @return right type of statement
	 * @throws SQLException
	 */
	private Statement getStatement(String sql) throws SQLException {
		Statement res;
		
		log.log(Level.FINE, "Getting the right statement.");
		
		Connection act = connection.getSwitcher().getConnection(sql);
		
		
		switch (resultSetCase) {
		case 2: res = act.createStatement(resultSetType, resultSetConcurrency); break;
		case 3: res = act.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability); break;
		default: res = act.createStatement();
		}
		
		return res;
	}
	
	/**
	 * This method sets current statement. It closes the previous one and opens new statement according to given sql
	 * 
	 * @param given SQL query
	 * @throws SQLException - if statement is closed or it is not possible to set the some values as there were in previous statement
	 */
	private void setCurrentStatement(String sql) throws SQLException {
		log.log(Level.INFO, "Setting new statement. Query: " + sql);
		
		if (statement != null) {
			if (statement.isClosed()) {
				String exc = "The statement is closed!";
				
				log.log(Level.SEVERE, exc);
				throw new SQLException(exc);
			}
			
			SQLWarning w;
			
			log.log(Level.FINE, "Saving warnings from old statement.");
			while ((w = statement.getWarnings()) != null) {
				warnings.add(w);
			}
			
			log.log(Level.FINE, "Closing old statement.");
			statement.close();
			
			
		}
		
		log.log(Level.FINE, "Establishing new statement.");
		statement = getStatement(sql);
		
		log.log(Level.FINE, "Setting values from old statement to new statement");
		
		if (maxFieldSet) {
			log.log(Level.FINE, "Setting maxField = " + maxFieldVal);
			
			statement.setMaxFieldSize(maxFieldVal);
		}
		
		if (maxRowSet) {
			log.log(Level.FINE, "Setting maxRow = " + maxRowVal);
			
			statement.setMaxRows(maxRowVal);
		}
		
		if (escapeProcessingSet) {
			log.log(Level.FINE, "Setting escapeProccesing = " + escapeProcessingVal);
			
			statement.setEscapeProcessing(escapeProcessingVal);
		}
		
		if (qTimeOutSet) {
			log.log(Level.FINE, "Setting queryTimeout = " + qTimeOutVal);
			
			statement.setQueryTimeout(qTimeOutVal);
		}
		
		if (cursorSet) {
			log.log(Level.FINE, "Setting cursorName = " + cursorVal);
			
			statement.setCursorName(cursorVal);
		}
		
		if (fetchDirSet) {
			log.log(Level.FINE, "Setting fetchDirection = " + fetchDirVal);
			
			statement.setFetchDirection(fetchDirVal);
		}
		
		if (fetchSizeSet) {
			log.log(Level.FINE, "Setting fetchSize = " + fetchSizeVal);
			
			statement.setFetchSize(fetchSizeVal);
		}
		
		if (poolableSet) {
			log.log(Level.FINE, "Setting poolable = " + poolableVal);
			
			statement.setPoolable(poolableVal);
		}
	}
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		log.log(Level.FINE, "Unwrapping iface");
		
		return statement.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		log.log(Level.FINE, "isWrapperFor");
		
		return statement.isWrapperFor(iface);
	}

	@Override
	public ResultSet executeQuery(String sql) throws SQLException {
		setCurrentStatement(sql);
		
		log.log(Level.INFO, "Executing query:" + sql);
		
		return statement.executeQuery(sql);
	}

	@Override
	public int executeUpdate(String sql) throws SQLException {
		setCurrentStatement(sql);
		
		log.log(Level.INFO, "Executing update:" + sql);
		
		return statement.executeUpdate(sql);
	}

	@Override
	public void close() throws SQLException {
		log.log(Level.INFO, "Closing proxy statement");
		
		statement.close();
		clearBatch();
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		log.log(Level.FINE, "Getting max field size");
		
		return statement.getMaxFieldSize();
	}

	@Override
	public void setMaxFieldSize(int max) throws SQLException {
		statement.setMaxFieldSize(max);
		
		log.log(Level.FINE, "Setting max field size = " + max);
		
		maxFieldVal = max;
		maxFieldSet = true;
		
	}

	@Override
	public int getMaxRows() throws SQLException {
		log.log(Level.FINE, "Getting max rows");
		
		return statement.getMaxRows();
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		log.log(Level.FINE, "Setting max rows = " + max);
		
		statement.setMaxRows(max);
		
		maxRowVal = max;
		maxRowSet = false;
	}

	@Override
	public void setEscapeProcessing(boolean enable) throws SQLException {
		log.log(Level.FINE, "Setting escape processing = " + enable);
		
		statement.setEscapeProcessing(enable);
		
		escapeProcessingVal = enable;
		escapeProcessingSet = true;
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		log.log(Level.FINE, "Getting query timeout.");
		
		return statement.getQueryTimeout();
	}

	@Override
	public void setQueryTimeout(int seconds) throws SQLException {
		log.log(Level.FINE, "Setting query timeout = " + seconds + " s.");
		
		statement.setQueryTimeout(seconds);
		
		qTimeOutVal = seconds;
		qTimeOutSet = true;
	}

	@Override
	public void cancel() throws SQLException {
		log.log(Level.INFO, "Cancelling statement.");
		
		statement.cancel();
		
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		SQLWarning res;
		
		if (warnings.isEmpty()) {
			log.log(Level.FINE, "Getting warnings - from statement.");
			
			res = statement.getWarnings();
		} else {
			log.log(Level.FINE, "Getting warnings - from history of previous statement.");
			
			res = warnings.poll();
		}
		
		return res;
	}

	@Override
	public void clearWarnings() throws SQLException {
		log.log(Level.FINE, "Clearing warnings");
		
		warnings.clear();
		statement.clearWarnings();
		
	}

	@Override
	public void setCursorName(String name) throws SQLException {
		log.log(Level.FINE, "Setting cursor name to val = " + name);
		
		statement.setCursorName(name);
		
		cursorVal = name;
		cursorSet = true;
	}

	@Override
	public boolean execute(String sql) throws SQLException {
		log.log(Level.INFO, "Executing sql(" + sql + ")");
		
		setCurrentStatement(sql);
		return statement.execute(sql);
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		log.log(Level.FINE, "Getting result set");
		return statement.getResultSet();
	}

	@Override
	public int getUpdateCount() throws SQLException {
		log.log(Level.FINE, "Getting update count");
		
		return statement.getUpdateCount();
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		log.log(Level.FINE, "Getting more results.");
		
		return statement.getMoreResults();
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		log.log(Level.FINE, "Setting fetch direction = " + direction);
		
		statement.setFetchDirection(direction);
		
		fetchDirVal = direction;
		fetchDirSet = true;
	}

	@Override
	public int getFetchDirection() throws SQLException {
		log.log(Level.FINE, "Getting fetch direction.");
		
		return statement.getFetchDirection();
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		log.log(Level.FINE, "Setting fetch size = " + rows);
		
		statement.setFetchSize(rows);
		
		fetchSizeVal = rows;
		fetchSizeSet = true;
		
	}

	@Override
	public int getFetchSize() throws SQLException {
		log.log(Level.FINE, "Getting fetch size");
		
		return statement.getFetchSize();
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		log.log(Level.FINE, "Getting result set concurrency");
		
		return resultSetConcurrency;
	}

	@Override
	public int getResultSetType() throws SQLException {
		log.log(Level.FINE, "Getting result set type.");
		
		return statement.getResultSetType();
	}

	@Override
	public void addBatch(String sql) throws SQLException {
		try {
			log.log(Level.INFO, "Adding batch: " + sql);
			CallableStatement s = connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
			batchList.add(s);
		} catch (SQLException e) {
			String exc = "Unable to add batch, orriginal message: " + e.getMessage();
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
	}

	@Override
	public void clearBatch() throws SQLException {
		String exc = new String();
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
		
	}

	@Override
	public int[] executeBatch() throws SQLException {
		int[] res = new int[batchList.size()];
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
		
		return res;
	}

	@Override
	public Connection getConnection() throws SQLException {
		log.log(Level.FINE, "Getting proxy connection.");
		
		return connection;
	}

	@Override
	public boolean getMoreResults(int current) throws SQLException {
		log.log(Level.FINE, "Getting more results : " + current);
		
		return statement.getMoreResults(current);
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		log.log(Level.FINE, "Getting generated keys.");
		
		return statement.getGeneratedKeys();
	}

	@Override
	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		log.log(Level.INFO, "Executing update with autoGeneratedKeys = " + autoGeneratedKeys);
		setCurrentStatement(sql);
		
		return statement.executeUpdate(sql, autoGeneratedKeys);
	}

	@Override
	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		log.log(Level.INFO, "Executing update with columnInedxes = " + columnIndexes);
		
		setCurrentStatement(sql);
		
		return statement.executeUpdate(sql, columnIndexes);
	}

	@Override
	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		log.log(Level.INFO, "Executing update with columnNames = " + columnNames);
		
		setCurrentStatement(sql);
		
		return statement.executeUpdate(sql, columnNames);
	}

	@Override
	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		log.log(Level.INFO, "Executing query with autoGeneratedKeys = " + autoGeneratedKeys);
		
		setCurrentStatement(sql);
		
		return statement.execute(sql, autoGeneratedKeys);
	}

	@Override
	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		log.log(Level.INFO, "Executing query with columnInedxes = " + columnIndexes);
		
		setCurrentStatement(sql);
		
		return statement.execute(sql, columnIndexes);
	}

	@Override
	public boolean execute(String sql, String[] columnNames) throws SQLException {
		log.log(Level.INFO, "Executing query with columnNames = " + columnNames);
		
		setCurrentStatement(sql);
		
		return statement.execute(sql, columnNames);
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		log.log(Level.FINE, "Getting result set holdability.");
		
		return resultSetHoldability;
	}

	@Override
	public boolean isClosed() throws SQLException {
		log.log(Level.FINE, "Checking whether statement is closed.");
		
		return statement.isClosed();
	}

	@Override
	public void setPoolable(boolean poolable) throws SQLException {
		log.log(Level.FINE, "Setting poolable = " + poolable);
		
		statement.setPoolable(poolable);
		
		poolableVal = poolable;
		poolableSet = true;
	}

	@Override
	public boolean isPoolable() throws SQLException {
		log.log(Level.FINE, "Checking whether statement is poolable.");
		
		return statement.isPoolable();
	}

	@Override
	public void closeOnCompletion() throws SQLException {
		log.log(Level.FINE, "close on completion");
		
		statement.closeOnCompletion();
		
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		log.log(Level.FINE, "Checking whether statement is closed on completion.");
		
		return statement.isCloseOnCompletion();
	}

}
