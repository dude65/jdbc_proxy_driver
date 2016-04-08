package org.fit.jdbc_proxy_driver.implementation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author ondra
 *
 */
public class Switcher {
	final private List<ConnectionUnit> connectList;
	final private ConnectionUnit defConnection;
	
	public Switcher(List<ConnectionUnit> connectList, ConnectionUnit defConnection) {
		this.connectList = connectList;
		this.defConnection = defConnection;
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection(String sql) throws SQLException {
		Connection res = null;
		boolean found = false;
		
		for(Iterator<ConnectionUnit> iterator = connectList.iterator(); iterator.hasNext();) {
			ConnectionUnit u = iterator.next();
			
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
