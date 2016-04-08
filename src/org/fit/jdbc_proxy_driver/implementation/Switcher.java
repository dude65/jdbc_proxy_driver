package org.fit.jdbc_proxy_driver.implementation;

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
	final private ConnectionUnit defConnection;
	
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
		
		return res;
	}
}
