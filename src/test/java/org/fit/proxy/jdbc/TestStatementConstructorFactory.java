package org.fit.proxy.jdbc;

import static org.junit.Assert.*;

import java.sql.CallableStatement;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestStatementConstructorFactory {
	private static ProxyConnection connection;
	private static ConnectionUnit connectionUnit;
	private static String QUERY = "SELECT 1";
	
	@BeforeClass
	public static void setupConnection() throws Exception {
		Properties properties = TestUtils.loadDefaultProperties();
		Driver driver = new ProxyDriver();
		connection = (ProxyConnection) driver.connect(null, properties);
		connectionUnit = connection.getDefaultConnection();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		connection.close();
	}

	private static void doTests(StatementConstructorFactory factory) throws SQLException {
		Statement statement = factory.createStatement(connectionUnit);
		CallableStatement batchStatement = factory.createBatchStatement(connection, QUERY);;
		
		assertNotNull(statement);
		assertNotNull(batchStatement);
	}
	
	@Test
	public void testEmptyConstructor() throws SQLException {
		StatementConstructorFactory factory = new StatementConstructorFactory();
		doTests(factory);
		
	}
	
	@Test
	public void testTwoValues() throws SQLException {
		StatementConstructorFactory factory = new StatementConstructorFactory(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		doTests(factory);
	}
	
	@Test
	public void testThreeValues() throws SQLException {
		StatementConstructorFactory factory = new StatementConstructorFactory(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		doTests(factory);
	}

}
