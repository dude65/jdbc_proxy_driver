package org.fit.proxy.jdbc;

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
import java.util.logging.Level;
import java.util.logging.Logger;


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
	private final static Logger log = Logger.getLogger(ProxyConnection.class.getName());
	
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
		log.log(Level.FINE, "loading properties data from file:" + propFile);
		
		Properties prop = new Properties();
		InputStream is = null;
		
		try {
			is = new FileInputStream(propFile);
			prop.load(is);
			
		} catch (IOException e) {
			String exc = "Properties data on path: " + propFile + " are not available.";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					String exc = "Unable to close properties file on path:" + propFile;
					
					log.log(Level.SEVERE, exc);
					throw new SQLException(exc);
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
		
		log.log(Level.FINE, "Loading data from properties. Loading number of connections");
		
		if (itemsString == null) {
			exc = "Unknown number of connections.";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		int items;
		
		try {
			items = Integer.parseInt(itemsString);
			
			if (items <= 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			exc = "Invalid number of connections: " + itemsString;
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		log.log(Level.FINE, "Adding connections to map");
		
		try {
			for (int i = 0; i < items; i++) {
				ConnectionUnit u = getUnit(prop, i);
				
				if (loaded.containsKey(u.getName())) {
					exc = "The name of database " + u.getName() + " is duplicated";
					
					log.log(Level.SEVERE, "Failed when trying to add connection to map.");
					throw new SQLException(exc);
				}
				
				loaded.put(u.getName(), u);
			}
		} catch (SQLException e) {
			exc = e.getMessage();
		}
		
		log.log(Level.FINE, "Adding connections to map completed. Resolving default connections");
		
		String defaultConn = prop.getProperty("default");
		ConnectionUnit def = null;
		
		if (defaultConn != null) {
			def = loaded.get(defaultConn);
			
			if (def == null) {
				if (!exc.isEmpty()) {
					exc += '\n';
				}
				
				log.log(Level.SEVERE, "Failed when trying to resolve default connection.");
				exc += "Unable to resolve default connection";
			}
		}
		
		if (!exc.isEmpty()) {
			log.log(Level.SEVERE, "Error occured when loading data from properties. Closing opened connections.");
			
			try {
				closeOpenedConnections(loaded);
			} catch (SQLException e) {
				exc += '\n' + e.getMessage();
			}
			
			log.log(Level.SEVERE, "Whole message: " + exc);
			throw new SQLException(exc);
		}
		
		log.log(Level.SEVERE, "Loding data from properties and connecting to databases was succesfull.");
		return new Switcher(loaded, def, prop);
	}
	
	/**
	 * Private method, that obtain connections from property file by a specified number 
	 * @param prop - properties
	 * @param i - specified number
	 * @return - connection unit with connection
	 * @throws SQLException - if data are incomplete or it is not possible to connect to a database, an exception is thrown 
	 */
	private static ConnectionUnit getUnit(Properties prop, int i) throws SQLException {
		log.log(Level.INFO, "Connecting to database with number " + i);
		
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
					log.log(Level.FINE, "Connecting to database " + i + " (" + name + "). User name or password is null, conecting by given url");
					
					c = DriverManager.getConnection(url);
				} else {
					log.log(Level.FINE, "Connecting to database " + i + " (" + name + ").");
					c = DriverManager.getConnection(url, user, password);
				}
				
				res = new ConnectionUnit(name, regexp, c);
			} catch (SQLException e) {
				String exc = "Cannot open a connection to a " + name + " database. Original message: " + e.getMessage();
				
				log.log(Level.SEVERE, exc);
				throw new SQLException(exc);
			} catch (ClassNotFoundException e) {
				String exc = "The driver in connection " + name + " was not found. The class is: " + driver;
				
				log.log(Level.SEVERE, exc);
				throw new SQLException(exc);
			}
		} else {
			String exc = "Unable to read data about db" + i + " connection. Some properties are missing";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		log.log(Level.INFO, "Connecting to database with number " + i + " (" + name + ") was succesful.");
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
				log.log(Level.FINE, "Closing connection " + entry.getKey());
				
				entry.getValue().getConnection().close();
			} catch (SQLException e) {
				if (error) {
					exc += '\n';
				} else {
					error = true;
				}
				
				log.log(Level.FINE, "Closing connection " + entry.getKey() + " failed.");
				exc += "Unable to close connection " + entry.getKey();
			}
		}
		
		if (error) {
			throw new SQLException(exc);
		}
	}
}
