package org.fit.proxy.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Ondřej Marek
 * 
 * This class gathers information about connected databases and provides switching between contexts according to SQL queries.
 *
 */
public class Switcher {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	
	private Map<String, ConnectionUnit> connectList;
	private ConnectionUnit defConnection;
	boolean open = true;
	private List<Connection> failedList = new LinkedList<>();
	private Properties properties;
	
	public Switcher(Map<String, ConnectionUnit> connectList, ConnectionUnit defConnection, Properties properties) {
		this.connectList = connectList;
		this.defConnection = defConnection;
		this.properties = properties;
		
		log.log(Level.INFO, "Switcher established. Connections: " + connectList + "\nDefault: " + defConnection);
	}
	/**
	 * This method is getter for all connection units
	 * @return - list of connection units
	 */
	public List<ConnectionUnit> getConnectionList() {
		log.log(Level.FINE, "Getting list of all connections.");
		
		return new LinkedList<ConnectionUnit>(connectList.values());
	}
	
	public ConnectionUnit getDefaultConnection() throws SQLException {
		log.log(Level.FINE, "Getting default connection.");
		
		if (defConnection == null) {
			String exc = "No default connection set!";
			
			log.log(Level.SEVERE, exc);
			
			throw new SQLException(exc);
		}
		
		return defConnection;
	}
	
	public Properties getProperties() {
		log.log(Level.FINE, "Getting connection properties");
		
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
		ConnectionUnit res = null;
		boolean found = false;
		
		log.log(Level.INFO, "Associating connection to sql:" + sql);
		
		for(Map.Entry<String, ConnectionUnit> entry : connectList.entrySet()) {
			ConnectionUnit u = entry.getValue();
			
			if (u.matches(sql)) {
				log.log(Level.FINE, "Connection " + u.getName() + "matches to sql: " + sql);
				
				if (!found) {
					found = true;
					res = u;
				} else {
					failedList.clear();
					failedList.add(res.getConnection());
					failedList.add(u.getConnection());
					
					String exc = "The sql query is ambigious. There are more suitable database connection (" + res.getName() +", " + u.getName() + ") to your request.";
					
					log.log(Level.SEVERE, exc);
					throw new SQLException(exc);
				}
			}
		}
		
		if (!found) {
			log.log(Level.FINE, "No database connection matches to sql query (" + sql + "). Checking for default connection.");
			
			if (defConnection != null) {
				log.log(Level.FINE, "Default connection (" + defConnection.getName() + ")was found. Associating tu query:" + sql);
				
				res = defConnection;
			} else {
				failedList.clear();
				
				String exc = "There is no suitable database to the sql query";
				
				log.log(Level.SEVERE, exc);
				throw new SQLException(exc);
			}
		}
		
		log.log(Level.INFO, "Associating sql query (" + sql + ") was successful. The chosen connection is " + res.getName());
		
		return res.getConnection();
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
		
		log.log(Level.FINE, "Getting connection by name");
		
		if (cu != null) {
			res = cu.getConnection();
			
			log.log(Level.FINE, "Connection named " + name + " was found");
		} else {
			String exc = "Cannot get database connection. Database connection named " + name + " does not exists.";
			
			log.log(Level.SEVERE, exc);
			
			throw new SQLException();
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
		
		log.log(Level.INFO, "Closing connections.");
		
		for (Map.Entry<String, ConnectionUnit> entry : connectList.entrySet()) {
			String name = entry.getKey();
			ConnectionUnit cu = entry.getValue();
			
			try {
				cu.getConnection().close();
				
				log.log(Level.FINE, "Closing connection " + cu.getName() + " was successful.");
			} catch (SQLException e) {
				
				exList.put(name, e);
				notClosed.add(cu.getConnection());
				
				log.log(Level.SEVERE, "Closing connection " + cu.getName() + " failed.");
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
			
			log.log(Level.SEVERE, reason);
			throw new SQLException(reason);
		}
		
		log.log(Level.INFO, "Closing connections was successful.");
		
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
		
		log.log(Level.INFO, "Setting default database");
		
		if (u != null) {
			defConnection = u;
		} else {
			failedList.clear();
			String exc = "Setting default connection failed. Database connection named " + name + " does not exists.";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
	}
	
	/**
	 * Sets default database connection to null.
	 */
	public void unsetDefaultDatabase() {
		log.log(Level.INFO, "Default connection is unset");
		
		defConnection = null;
	}
	
	/**
	 * Returns the list of failed connections from the last time, when exception was thrown. It is useful when handling exception.
	 * 
	 * @return - the list of failed connections (new object), if no connection ever failed, then returns empty list
	 */
	@Deprecated
	public List<Connection> getFailedConnections() {
		log.log(Level.INFO, "Getting list of failed connections.");
		
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
			String exc = "There was an attempt to close database connections but it was not successfull. Use method getFailedConnection to get a list of them.";
			
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		log.log(Level.FINE, "isClosed = " + !open);
		
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
