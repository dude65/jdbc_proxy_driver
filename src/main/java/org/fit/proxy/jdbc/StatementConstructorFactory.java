package org.fit.proxy.jdbc;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.fit.proxy.jdbc.exception.ProxyException;

/**
 * This class collects result set properties and creates statements from given connection
 * @author Ondřej Marek
 */
public class StatementConstructorFactory {
	public static final int PROPERTY_UNSET = -1;
	
	private final int resultSetType;
	private final int resultSetConcurrency;
	private final int resultSetHoldability;
	private final Integer[] values;
	private final Class<Integer>[] valueClass;
	private final Class<Object>[] batchClass;
	
	@SuppressWarnings("unchecked")
	public StatementConstructorFactory() {
		this.resultSetType = PROPERTY_UNSET;
		this.resultSetConcurrency = PROPERTY_UNSET;
		this.resultSetHoldability = PROPERTY_UNSET;
		
		this.values = new Integer[0];
		this.valueClass = new Class[0];
		this.batchClass = new Class[] {String.class};
	}

	@SuppressWarnings("unchecked")
	public StatementConstructorFactory(int resultSetType, int resultSetConcurrency) {
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;
		this.resultSetHoldability = PROPERTY_UNSET;
		
		this.values = new Integer[] {resultSetType, resultSetConcurrency};
		this.valueClass = new Class[] {int.class, int.class};
		this.batchClass = new Class[] {String.class, int.class, int.class};
	}



	@SuppressWarnings("unchecked")
	public StatementConstructorFactory(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		this.resultSetType = resultSetType;
		this.resultSetConcurrency = resultSetConcurrency;
		this.resultSetHoldability = resultSetHoldability;
		
		this.values = new Integer[] {resultSetType, resultSetConcurrency, resultSetHoldability};
		
		this.valueClass = new Class[] {int.class, int.class, int.class};
		this.batchClass = new Class[] {String.class, int.class, int.class, int.class};
		
	}
	
	/**
	 * This method creates the right statement according to the values of result set type, concurrency and holdability
	 * @param connectionUnit connection where to create the statement
	 * @return statement
	 * @throws SQLException error occurs
	 */
	public Statement createStatement(ConnectionUnit connectionUnit) throws SQLException {
		Connection connection = connectionUnit.getConnection();
		Method method;
		try {
			method = connection.getClass().getMethod("createStatement", valueClass);
			return (Statement) method.invoke(connection,(Object[]) values);
		} catch (Exception e) {
			throw new ProxyException("Unable to create statement in connection " + connectionUnit.getName(), e, connectionUnit);
		}
	}
	
	/**
	 * This method creates the right callable statement (batch statement) according to the values of result set type, concurrency and holdability
	 * @param connection proxy connection
	 * @return callable statement
	 * @throws SQLException error occurs
	 */
	public CallableStatement createBatchStatement(ProxyConnection connection, String sql) throws SQLException {
		Method method;
		try {
			Object[] params = new Object[values.length + 1];
			params[0] = sql;
			
			for (int i = 0; i < values.length; i++) {
				params[i + 1] = values[i];
			}
			
			method = connection.getClass().getMethod("prepareCall", batchClass);
			return (CallableStatement) method.invoke(connection, params);
		} catch (Exception e) {
			throw new SQLException("Unable to create batch statement.", e);
		}
	}

	public int getResultSetType() {
		return resultSetType;
	}

	public int getResultSetConcurrency() {
		return resultSetConcurrency;
	}

	public int getResultSetHoldability() {
		return resultSetHoldability;
	}	
}
