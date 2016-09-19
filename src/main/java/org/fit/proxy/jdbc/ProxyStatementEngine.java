package org.fit.proxy.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

/**
 * This classed is used by proxy statement as its engine.
 * @author Ondřej Marek
 */
public class ProxyStatementEngine implements IConnectionEnsure {
	private final ProxyConnection proxyConnection;
	private final StatementConstructorFactory statementFactory;
	private Statement statement;

	private final ProxyProperiesHelper propertiesHelper = new ProxyProperiesHelper(this);

	public ProxyStatementEngine(ProxyConnection connection, StatementConstructorFactory statementFactory) throws SQLException {
		this.proxyConnection = connection;
		this.statementFactory = statementFactory;
		
		initiateStatement();
	}

	@Override
	public void ensureConnectionIsAlive() throws SQLException {
		if (proxyConnection.isClosed() || statement.isClosed()) {
			throw new SQLException("Proxy statement is already closed!");
		}
	}

	public Statement getStatement() {
		return statement;
	}

	/**
	 * Creates a new statement in the right connection
	 * @param sql of statement
	 * @return the correct statement
	 * @throws SQLException connection closed or error when reflecting set variables to new statement
	 */
	public Statement getStatement(String sql) throws SQLException {
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

	public ProxyProperiesHelper getPropertiesHelper() {
		return propertiesHelper;
	}
}
