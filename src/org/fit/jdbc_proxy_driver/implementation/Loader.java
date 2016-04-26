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
 * This class is responsible for loading informations about databases
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
			try {
				is.close();
			} catch (IOException e) {
				throw new SQLException("Unable to close properties file");
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
	
	private static ConnectionUnit getUnit(Properties prop, int i) throws SQLException {
		ConnectionUnit res = null;
		
		String driver = prop.getProperty("db" + i + "_driver");
		String url = prop.getProperty("db" + i + "_url");
		String regexp = prop.getProperty("db" + i + "_property");
		String name = prop.getProperty("db" + i + "_name");
		String user = prop.getProperty("db" + i + "_user");
		String password = prop.getProperty("db" + i + "_password");
		
		if (driver != null && url != null && regexp != null) {
			try {
				Class.forName(driver);
				Connection c;
				
				if (name == null || user == null || password == null) {
					c = DriverManager.getConnection(url);
				} else {
					c = DriverManager.getConnection(url, user, password);
				}
				
				res = new ConnectionUnit(name, regexp, c);
			} catch (SQLException e) {
				
			} catch (ClassNotFoundException e) {
				throw new SQLException("The driver in connection " + name + " was not found.");
			}
		} else {
			throw new SQLException("Unable to read data about db" + i + " connection.");
		}
		
		return res;
	}
	
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
