package org.fit.proxy.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

/**
 * This classed is used by proxy statement as its engine.
 * @author Ondřej Marek
 */
public class ProxyStatementEngine implements IConnectionEnsure {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());

	private final ProxyConnection proxyConnection;
	private final StatementConstructorFactory statementFactory;
	private final ProxyStatementBatcher batcher;
	private Statement statement;

	private final ProxyProperiesHelper propertiesHelper = new ProxyProperiesHelper(this);

	public ProxyStatementEngine(ProxyConnection connection, StatementConstructorFactory statementFactory) throws SQLException {
		this.proxyConnection = connection;
		this.statementFactory = statementFactory;
		this.batcher = new ProxyStatementBatcher(this.proxyConnection, this.statementFactory);

		initiateStatement();
	}

	@Override
	public void ensureConnectionIsAlive() throws SQLException {
		if (isClosed()) {
			throw new SQLException("Proxy statement is already closed!");
		}
	}

	private boolean isClosed() {
        try {
            return proxyConnection.isClosed() || statement.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
	
	public boolean isStatementClosed() throws SQLException {
		return statement.isClosed();
	}

	public Statement getStatement() throws SQLException {
		ensureConnectionIsAlive();
		
		return statement;
	}

	/**
	 * Creates a new statement in the right connection
	 * @param sql of statement
	 * @return the correct statement
	 * @throws SQLException connection closed or error when reflecting set variables to new statement
	 */
	public Statement getStatement(String sql) throws SQLException {
		ensureConnectionIsAlive();
		
		if (statement != null) {
			statement.close();
		}
		
		statement = statementFactory.createStatement(proxyConnection.getConnectionBySql(sql));

		try {
			for (Iterator<ConnectionPropertiesUnit> iterator = propertiesHelper.iterator(); iterator.hasNext();) {
				ConnectionPropertiesUnit property = iterator.next();

				String setMethodName = "set" + StringUtils.capitalize(property.getName());
				Method setMethod = statement.getClass().getMethod(setMethodName, property.getValue().getClass());

				setMethod.invoke(statement, property.getValue());
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new SQLException("An error occured when reflecting proxy statement properties to new statement." +
				"Statement will work but it does not have to have the same behavior.", e);
		}

		return statement;
	}
	
	private final void initiateStatement() throws SQLException {
		ConnectionUnit connection = proxyConnection.getDefaultConnection();
		
		if (connection == null) {
			connection = proxyConnection.getConnectionList().get(0);
		}
		
		statement = statementFactory.createStatement(connection);
	}

	public ProxyProperiesHelper getPropertiesHelper() throws SQLException {
		ensureConnectionIsAlive();
		return propertiesHelper;
	}
	
	public ProxyConnection getConnection() throws SQLException {
		ensureConnectionIsAlive();
		return proxyConnection;
	}

	public void addBatch(String sql) throws SQLException {
		ensureConnectionIsAlive();
		batcher.addBatch(sql);
	}

	public void clearBatch() throws SQLException {
		ensureConnectionIsAlive();
		batcher.clearBatch();
	}

	public int[] executeBatch() throws SQLException {
		ensureConnectionIsAlive();
		return batcher.executeBatch();
	}

	public void close() {
	    if (isClosed()) {
	        return;
        }

		batcher.safeClose();

		try {
			statement.close();
		} catch (SQLException e) {
			log.log(Level.WARNING, "Problem closing statement", e);
		}
	}
}
