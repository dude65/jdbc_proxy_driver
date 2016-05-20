package org.fit.proxy.jdbc;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Ondřej Marek
 * 
 * This class represents the proxy driver itself. The method connect accepts two parameters: path to properties file and properties itself.
 * If both are null, then it looks for a properties file config.properties in current directory.
 * If both are specified, then it looks for a file specified by a given path.
 * If only one of these two parameters is not null, it picks the right parameter.
 * 
 * To see, how to write a correct properties file, look into a Loader javadoc.
 * 
 *
 */
public class ProxyDriver implements Driver{
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	
	/**
	 * It parses given URL with prefix and returns the path to properties file
	 * 
	 * @param url with prefix
	 * @return path to properties file
	 * @throws SQLException
	 */
	private static String parseUrl(String url) throws SQLException {
		log.log(Level.FINE, "Parsing url.");
		
		if (url == null) {
			log.log(Level.FINE, "url = null");
			
			return null;
		}
		
		String res = null;
		String prefix = "jdbc:proxy:";
		
		if (StringUtils.startsWith(url, prefix)) {
			res = StringUtils.substring(url, prefix.length());
			
			log.log(Level.FINE, "url parsed succesfully");
		} else {
			String exc = "Error while parsing url.";
			
			log.log(Level.SEVERE, exc);
			throw new SQLException(exc);
		}
		
		return res;
	}
	
	@Override
	public boolean acceptsURL(String url) throws SQLException {
		boolean res;
		
		log.log(Level.FINE, "acceptsURL started");
		
		try {
			url = parseUrl(url);
		} catch (SQLException e) {
			url = null;
		}
		
		try {
			Paths.get(url);
			res = true;
		} catch (InvalidPathException | NullPointerException e) {
			res = false;
		}
		
		log.log(Level.INFO, "acceptsURL:" + url + " = " + res);
		
		return res;
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		log.log(Level.INFO, "Connecting..");
		
		Switcher s;
		url = parseUrl(url);
		
		if (url == null && info == null) {
			log.log(Level.FINE, "url and properties null => calling Switcher s = Loader.loadData()");
			
			s = Loader.loadData();
		} else if (url == null) {
			log.log(Level.FINE, "url is null => calling Switcher s = Loader.loadData(info); (info are properties)");
			
			s = Loader.loadData(info);
		} else {
			log.log(Level.FINE, "url is filled => calling Switcher s = Loader.loadData(url);");
			
			s = Loader.loadData(url);
		}
		
		
		log.log(Level.INFO, "Succesfully connected");
		
		return new ProxyConnection(s);
	}

	@Override
	public int getMajorVersion() {
		return 1;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return log;
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		log.log(Level.INFO, "Getting driver property info");
		
		int items = 2;
		
		try {
			url = parseUrl(url);
		} catch (SQLException e) {
			url = null;
		}
		
		
		if (info == null) {
			log.log(Level.FINE, "given properties are null => setting empty properties");
			
			info = new Properties();
		}
		
		if (url != null) {
			try {
				log.log(Level.FINE, "url is not null - trying to load properties from given url");
				
				info.clear();
				info.load(new FileInputStream(url));
			} catch (IOException e) {
				String exc = "Cannot load properties file from given url.";
				
				log.log(Level.SEVERE, exc);
				throw new SQLException(exc);
			}
		}
		
		
		if (info.containsKey("items")) {
			try {
				log.log(Level.FINE, "Number of database connection (items) is contained in properties file - trying to parse the number");
				
				items = Integer.parseInt(info.getProperty("items"));
				
				if (items <= 0) {
					log.log(Level.FINE, "Number is lesser or equal to zero. Setting default value: 2");
					items = 2;
				}
				
				log.log(Level.FINE, "Number parsed succesfully");
				
			} catch (NumberFormatException e) {
				log.log(Level.FINE, "Number parsed unsuccesfully. The number remains default: 2");
			}
		}
		
		log.log(Level.FINE, "Setting driver property info.");
		
		DriverPropertyInfo[] res = new DriverPropertyInfo[2 + 6 * items];
		
		res[0] = new DriverPropertyInfo("items", new Integer(items).toString());
		res[1] = new DriverPropertyInfo("default", emptyIfNull(info.getProperty("default")));
		
		res[0].required = true;
		res[1].required = false;
		
		res[0].description = "Number of connnections to databases.";
		res[1].description = "Specifies primary connection by name.";
		
		for (int i = 0; i < items; i++) {
			res[i * 6 + 2] = new DriverPropertyInfo("db" + i + "_driver", emptyIfNull(info.getProperty("db" + i + "_driver")));
			res[i * 6 + 2].required = true;
			res[i * 6 + 2].description = "Specifies the class for database driver.";
			
			res[i * 6 + 3] = new DriverPropertyInfo("db" + i + "_url", emptyIfNull(info.getProperty("db" + i + "_url")));
			res[i * 6 + 3].required = true;
			res[i * 6 + 3].description = "Specifies the database url. It may contains additional information such as user name or password.";
			
			res[i * 6 + 4] = new DriverPropertyInfo("db" + i + "_name", emptyIfNull(info.getProperty("db" + i + "_name")));
			res[i * 6 + 4].required = true;
			res[i * 6 + 4].description = "Unique name to mark database connection.";
			
			res[i * 6 + 5] = new DriverPropertyInfo("db" + i + "_user", emptyIfNull(info.getProperty("db" + i + "_user")));
			res[i * 6 + 5].required = false;
			res[i * 6 + 5].description = "Specifies the database user name.";
			
			res[i * 6 + 6] = new DriverPropertyInfo("db" + i + "_password", emptyIfNull(info.getProperty("db" + i + "_password")));
			res[i * 6 + 6].required = false;
			res[i * 6 + 6].description = "Specifies the password to database.";
			
			res[i * 6 + 7] = new DriverPropertyInfo("db" + i + "_regexp", emptyIfNull(info.getProperty("db" + i + "_regexp")));
			res[i * 6 + 7].required = true;
			res[i * 6 + 7].description = "Specifies regular expression that is going to be associated to this connection.";
		}
		
		log.log(Level.FINE, "Driver property info was set succesfully.");
		
		return res;
	}
	
	/**
	 * Returns the same string, but when is null, it returns empty String
	 * 
	 * @param str
	 * @return same string
	 */
	private static String emptyIfNull(String str) {
		if (str == null) {
			str = new String();
			
			log.log(Level.FINE, "Property was null, setting an empty string");
		}
		
		return str;
	}
	@Override
	public boolean jdbcCompliant() {
		return false;
	}
}
