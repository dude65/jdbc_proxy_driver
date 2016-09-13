package org.fit.proxy.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.fit.proxy.jdbc.exception.ProxyException;

/**
 * @author Ondřej Marek
 * 
 * This class gathers information about connected databases and provides switching between contexts according to SQL queries.
 *
 */
public class Switcher {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	
	private Map<String, ConnectionUnit> connectionList;
	private ConnectionUnit defaultConnection;
	
	@Deprecated //TODO move to engine
	private Properties properties;
	
	public Switcher(Map<String, ConnectionUnit> connectionList, ConnectionUnit defaultConnection, Properties properties) {
		this.connectionList = connectionList;
		this.defaultConnection = defaultConnection;
		this.properties = properties;
		
		log.fine("Switcher established. Connections: " + connectionList + "\nDefault: " + defaultConnection);
	}
	/**
	 * This method is getter for all connection units
	 * @return - list of connection units
	 */
	public List<ConnectionUnit> getConnectionList() {		
		return new LinkedList<ConnectionUnit>(connectionList.values());
	}
	
	public ConnectionUnit getDefaultConnection() throws SQLException {
		if (defaultConnection == null) {
			String exc = "No default connection is set!";			
			throw new SQLException(exc);
		}
		
		return defaultConnection;
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
		log.fine(new StringBuilder().append("Starting to associate connection to sql :").append(sql).toString());
		
		ConnectionUnit result = associateConnection(sql);
		result = checkResult(result, sql);
		
		return result.getConnection();
	}
	
	private ConnectionUnit associateConnection(String sql) throws SQLException {
		ConnectionUnit result = null;
		
		for (Entry<String, ConnectionUnit> entry : connectionList.entrySet()) {
			ConnectionUnit connection = entry.getValue();
			
			if (connection.matches(sql)) {
				log.fine(new StringBuilder("Connection ").append(connection.getName()).append(" matches to sql query:").append(sql).toString());
				
				if (result != null) {
					String errMessage = new StringBuilder("The sql query").append(sql).append(" is ambigious. More databases matches.").toString();
					log.fine(errMessage);
					
					SQLException exception = new SQLException(errMessage);
					
					String message = "Ambigious connection.";
					exception.setNextException(new ProxyException(message, result));
					exception.setNextException(new ProxyException(message, connection));
					
					throw exception;
				}
				
				result = connection;
			}
		}
		
		return result;
	}
	
	private ConnectionUnit checkResult(ConnectionUnit chosen, String sql) throws SQLException {
		if (chosen == null) {
			if (defaultConnection != null) {
				log.fine(new StringBuilder("Sql query does not match to any connection, but default connection (").append(defaultConnection.getName()).append(") is set. Sql query: ").append(sql).toString());
				chosen = defaultConnection;
			} else {
				log.fine(new StringBuilder("Sql query was not associated to any connection and default connection is unset. Sql query: ").append(sql).toString());
				throw new SQLException(new StringBuilder("There is no suitable database for sql query: ").append(sql).toString());
			}
		} else {
			log.fine(new StringBuilder("Connection ").append(chosen.getName()).append(" was associated to query ").append(sql).toString());
		}
		
		return chosen;
	}
	
	/**
	 * Returns specified connection by name
	 * @param name connection name
	 * @return connection or null if not found
	 */
	public ConnectionUnit getConnectionByName(String name) {		
		return connectionList.get(name);
	}
	
	/**
	 * Sets default database connection specified by name. When an exception is thrown then the default connection will not change.
	 * 
	 * @param name of connection
	 * @throws SQLException - name of connection does not match to any connection
	 */
	public void setDefaultDatabase(String name) throws SQLException {
		ConnectionUnit newDefault = connectionList.get(name);
		
		if (newDefault != null) {
			defaultConnection = newDefault;
		} else {
			String message = new StringBuilder("Cannot set up default connection ").append(name).append(". This connection does not exists.").toString();
			throw new SQLException(message);
		}
	}
	
	/**
	 * Sets default database connection specified by connection unit.
	 * @param connection connection to make default
	 * @throws SQLException when connection is not present in connection map or is null
	 */
	public void setDefaultDatabase(ConnectionUnit connection) throws SQLException {
		if (connection == null) {
			throw new SQLException("No default connection specified. If you wanted to unset default connection, please use method unsetDefaultDatabase instead.");
		}
		
		if (connectionList.containsKey(connection)) {
			defaultConnection = connection;
		} else {
			String message = new StringBuilder("Unknown connection: ").append(connection.getName()).append(". This connection cannot be set.").toString();
			throw new ProxyException(message, connection);
		}
	}
	
	/**
	 * Sets default database connection to null.
	 */
	public void unsetDefaultDatabase() {
		defaultConnection = null;
	}
	
	@Override
	public String toString() {
		return connectionList.toString() + "/nDefault: " + ((defaultConnection == null) ? "null" : defaultConnection.toString());
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(99, 1171).append(defaultConnection).toHashCode();
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
		
		return new EqualsBuilder().append(connectionList, oth.connectionList).append(defaultConnection, oth.defaultConnection).isEquals();
	}
}
