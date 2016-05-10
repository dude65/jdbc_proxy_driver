package org.fit.jdbc_proxy_driver.implementation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;


/**
 * 
 * @author Ondřej Marek
 *
 * This class is responsible for loading informations about databases from given properties.
 * 
 * An example of correct written properties file:
 * items=2
 * db0_driver=com.mysql.jdbc.Driver
 * db0_url=jdbc:mysql://localhost/proxyDatabase1
 * db0_name=MyDatabase1
 * db0_user=root
 * db0_password=root
 * db0_regexp=^CREATE*
 * db1_driver=com.mysql.jdbc.Driver
 * db1_url=jdbc:mysql://localhost/proxyDatabase2?user=dude65&password=12345
 * db1_name=MyDatabase2
 * db1_regexp=^SELECT*
 * default=MyDatabase2
 * 
 * items (compulsory) - number of databases to connect
 * dbX_driver (compulsory) - class of driver for database connections
 * dbX_url (compulsory) - url of database to connect
 * dbX_name (compulsory) - name of how do you wish to name this connection
 * dbX_user (optional) - database user
 * dbX_password (optional) - database password
 * dbX_regexp (compulsory) - regular expression associated to the connection
 * default (optional) - name of database to which should be oriented all sql queries that are not associated
 * 
 * It is not allowed to have two database connections with the same name
 */
public class Loader {
	
	/**
	 * This method is called to obtain object Switcher with a collection of database connections.
	 * When no file specified, it is looking for config.properties file in current directory
	 * 
	 * @return Switcher with a collection of database connections
	 * @throws SQLException if data are not correct
	 */
	public static Switcher loadData() throws SQLException {
		return loadData("./config.properties");
	}
	
	/**
	 * This method is called to obtain object Switcher with a collection of database connections.
	 * 
	 * @param propFile - a path to property file
	 * @return Switcher with a collection of database connections
	 * @throws SQLException if data are not correct
	 */
	public static Switcher loadData(String propFile) throws SQLException {
		Properties prop = new Properties();
		InputStream is = null;
		
		try {
			is = new FileInputStream(propFile);
			prop.load(is);
			
		} catch (IOException e) {
			throw new SQLException("Properties data are missing");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new SQLException("Unable to close properties file");
				}
			}
			
		}
		
		return loadData(prop);
	}
	
	/**
	 * This method is called to obtain object Switcher with a collection of database connections.
	 * 
	 * @param prop - property file with informations
	 * @return Switcher with a collection of database connections
	 * @throws SQLException if data are not correct
	 */
	public static Switcher loadData(Properties prop) throws SQLException {
		Map<String, ConnectionUnit> loaded = new HashMap<String, ConnectionUnit>();
		String itemsString = prop.getProperty("items");
		String exc = new String();
		
		if (itemsString == null) {
			throw new SQLException("Unknown number of connections.");
		}
		
		int items = Integer.parseInt(itemsString);
		
		if (items <= 0) {
			throw new SQLException("Invalid number of connections: " + items);
		}
		
		try {
			for (int i = 0; i < items; i++) {
				ConnectionUnit u = getUnit(prop, i);
				
				if (loaded.containsKey(u.getName())) {
					throw new SQLException("The name of database " + u.getName() + " is duplicated");
				}
				
				loaded.put(u.getName(), u);
			}
		} catch (SQLException e) {
			exc = e.getMessage();
		}
		
		String defaultConn = prop.getProperty("default");
		ConnectionUnit def = null;
		
		if (defaultConn != null) {
			def = loaded.get(defaultConn);
			
			if (def == null) {
				if (!exc.isEmpty()) {
					exc += '\n';
				}
				
				exc += "Unable to resolve default connection";
			}
		}
		
		if (!exc.isEmpty()) {
			try {
				closeOpenedConnections(loaded);
			} catch (SQLException e) {
				exc += '\n' + e.getMessage();
			}
			
			throw new SQLException(exc);
		}
		
		return new Switcher(loaded, def);
	}
	
	/**
	 * Private method, that obtain connections from property file by a specified number 
	 * @param prop - properties
	 * @param i - specified number
	 * @return - connection unit with connection
	 * @throws SQLException - if data are incomplete or it is not possible to connect to a database, an exception is thrown 
	 */
	private static ConnectionUnit getUnit(Properties prop, int i) throws SQLException {
		ConnectionUnit res = null;
		
		String driver = prop.getProperty("db" + i + "_driver");
		String url = prop.getProperty("db" + i + "_url");
		String name = prop.getProperty("db" + i + "_name");
		String user = prop.getProperty("db" + i + "_user");
		String password = prop.getProperty("db" + i + "_password");
		String regexp = prop.getProperty("db" + i + "_regexp");
		
		if (driver != null && url != null && regexp != null && name != null) {
			try {
				Class.forName(driver);
				Connection c;
				
				if (user == null || password == null) {
					c = DriverManager.getConnection(url);
				} else {
					c = DriverManager.getConnection(url, user, password);
				}
				
				res = new ConnectionUnit(name, regexp, c);
			} catch (SQLException e) {
				throw new SQLException("Cannot open a connection to a " + name + " database. Original message: " + e.getMessage());
			} catch (ClassNotFoundException e) {
				throw new SQLException("The driver in connection " + name + " was not found.");
			}
		} else {
			throw new SQLException("Unable to read data about db" + i + " connection.");
		}
		
		return res;
	}
	
	/**
	 * This private method is called if connecting to databases fails. It closes all opened connections.
	 * @param connections
	 * @throws SQLException - if some connections cannot be closed, an exception is thrown 
	 */
	private static void closeOpenedConnections(Map<String, ConnectionUnit> connections) throws SQLException {
		String exc = new String();
		boolean error = false;
		
		for (Entry<String, ConnectionUnit> entry : connections.entrySet()) {
			try {
				entry.getValue().getConnection().close();
			} catch (SQLException e) {
				if (error) {
					exc += '\n';
				} else {
					error = true;
				}
				
				exc += "Unable to close connection " + entry.getKey();
			}
		}
		
		if (error) {
			throw new SQLException(exc);
		}
	}
}
