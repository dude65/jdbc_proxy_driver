package org.fit.jdbc_proxy_driver.implementation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * 
 * @author Ondřej Marek
 * 
 * This class gathers information about connected databases and provides switching between contexts according to SQL queries.
 *
 */
public class Switcher {
	final private Map<String, ConnectionUnit> connectList;
	private ConnectionUnit defConnection;
	
	public Switcher(Map<String, ConnectionUnit> connectList, ConnectionUnit defConnection) {
		this.connectList = connectList;
		this.defConnection = defConnection;
	}
	
	/**
	 * This method is used by Driver. It iterates through list of database and checks if they matches to the SQL query. Ideally it finds only one match.
	 * If there is no match then it returns default connection or an exception is thrown if there is no default connection set.
	 * If there are more matches the an exception is thrown as well.
	 * 
	 * 
	 * @param sql - string of a SQL query
	 * @return Connection - Returns that is connected to the SQL query
	 * @throws SQLException - If there is no database that suits to query and there is no default database set or if there are more suitable databases, the exception is thrown
	 */
	public Connection getConnection(String sql) throws SQLException {
		Connection res = null;
		boolean found = false;
		
		for(Map.Entry<String, ConnectionUnit> entry : connectList.entrySet()) {
			ConnectionUnit u = entry.getValue();
			
			if (u.matches(sql)) {
				if (!found) {
					found = true;
					res = u.getConnection();
				} else {
					throw new SQLException("The sql query is ambigious. There are more suitable database connection to your request.");
				}
			}
		}
		
		if (!found) {
			if (defConnection != null) {
				res = defConnection.getConnection();
			} else {
				throw new SQLException("There is no suitable database to the sql query");
			}
		}
		
		try {
			@SuppressWarnings("resource")
			FileOutputStream f = new FileOutputStream("test.txt", true);
			byte [] toWrite = new byte[2];
			toWrite[0] = (found) ? (byte)1 : (byte) 0;
			toWrite[1] = (byte) '\n';
			
			f.write(toWrite);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
	}
	
	/**
	 * Returns connection specified by name.
	 * 
	 * @param name of connection
	 * @return connection by name
	 * @throws SQLException - name of connection does not match to any connection
	 */
	public Connection getConnectionByName(String name) throws SQLException {
		ConnectionUnit cu = connectList.get(name);
		Connection res = null;
		
		if (cu != null) {
			res = cu.getConnection();
		} else {
			throw new SQLException("Cannot get database connection. Database connection named " + name + " does not exists.");
		}
		
		return res;
	}
	
	/**
	 * Sets default database connection specified by name. When an exception is thrown then the default connection will not change.
	 * 
	 * @param name of connection
	 * @throws SQLException - name of connection does not match to any connection
	 */
	public void setDefaultDatabase(String name) throws SQLException {
		ConnectionUnit u = connectList.get(name);
		
		if (u != null) {
			defConnection = u;
		} else {
			throw new SQLException("Cannot set default database connection. Database connection named " + name + " does not exists.");
		}
	}
	
	/**
	 * Sets default database connection to null.
	 */
	public void unsetDefaultDatabase() {
		defConnection = null;
	}
}
