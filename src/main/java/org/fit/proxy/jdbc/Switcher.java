package org.fit.proxy.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Ondřej Marek
 * 
 * This class gathers information about connected databases and provides switching between contexts according to SQL queries.
 *
 */
public class Switcher {
	final private Map<String, ConnectionUnit> connectList;
	private ConnectionUnit defConnection;
	boolean open = true;
	private List<Connection> failedList = new LinkedList<>();
	private Properties properties;
	
	public Switcher(Map<String, ConnectionUnit> connectList, ConnectionUnit defConnection, Properties properties) {
		this.connectList = connectList;
		this.defConnection = defConnection;
		this.properties = properties;
	}
	/**
	 * This method is getter for all connection units
	 * @return - list of connection units
	 */
	public List<ConnectionUnit> getConnectionList() {
		return new LinkedList<ConnectionUnit>(connectList.values());
	}
	
	public ConnectionUnit getDefaultConnection() throws SQLException {
		if (defConnection == null) {
			throw new SQLException("No default connection set!");
		}
		
		return defConnection;
	}
	
	public Properties getProperties() {
		return properties;
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
					failedList.clear();
					failedList.add(res);
					failedList.add(u.getConnection());
					
					throw new SQLException("The sql query is ambigious. There are more suitable database connection to your request.");
				}
			}
		}
		
		if (!found) {
			if (defConnection != null) {
				res = defConnection.getConnection();
			} else {
				failedList.clear();
				throw new SQLException("There is no suitable database to the sql query");
			}
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
	 * It closes all connections.
	 * @throws SQLException - when some connections fails to close, then the exception with names of connections and descriptions of exception are thrown.
	 */
	public void closeConnections() throws SQLException {
		Map<String, SQLException> exList = new TreeMap<>();
		List<Connection> notClosed = new LinkedList<>();
		
		for (Map.Entry<String, ConnectionUnit> entry : connectList.entrySet()) {
			String name = entry.getKey();
			Connection c = entry.getValue().getConnection();
			
			try {
				c.close();
			} catch (SQLException e) {
				
				exList.put(name, e);
				notClosed.add(c);
			}	
		}
		
		if (exList.size() > 0) {
			failedList.clear();
			failedList.addAll(notClosed);
			
			String reason = new String();
			
			reason += "These connections cannot be closed:\n";
			
			for (Map.Entry<String, SQLException> entry : exList.entrySet()) {
				reason += '\n' + entry.getKey() + ": " + entry.getValue().getMessage();
			}
			
			throw new SQLException(reason);
		}
		
		open = false;
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
			failedList.clear();
			throw new SQLException("Cannot set default database connection. Database connection named " + name + " does not exists.");
		}
	}
	
	/**
	 * Sets default database connection to null.
	 */
	public void unsetDefaultDatabase() {
		defConnection = null;
	}
	
	/**
	 * Returns the list of failed connections from the last time, when exception was thrown. It is useful when handling exception.
	 * 
	 * @return - the list of failed connections (new object), if no connection ever failed, then returns empty list
	 */
	public List<Connection> getFailedConnections() {
		return new LinkedList<Connection>(failedList);
	}
	
	/**
	 * Test if connections are closed.
	 * 
	 * @return - returns true if the closeConnections() was called
	 * @throws SQLException - when some databases were not closed, the exception is thrown
	 */
	public boolean isClosed() throws SQLException {
		if (!(open || failedList.isEmpty())) {
			throw new SQLException("There was an attempt to close database connections but it was not successfull. Use method getFailedConnection to get a list of them.");
		}
		
		return !open;
	}
	
	@Override
	public String toString() {
		return connectList.toString() + "/nDefault: " + ((defConnection == null) ? "null" : defConnection.toString());
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(99, 1171).append(defConnection).toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj == this) {
			return true;
		}
		
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		
		Switcher oth = (Switcher) obj;
		
		return new EqualsBuilder().append(connectList, oth.connectList).append(defConnection, oth.defConnection).isEquals();
	}
}
