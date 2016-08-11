package org.fit.proxy.jdbc;

import java.io.File;
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
public class ProxyDriver implements Driver {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	private final static String URL_PREFIX = "jdbc:proxy:";
	/**
	 * It parses given URL with prefix and returns the path to properties file
	 * 
	 * @param url with prefix
	 * @return path to properties file or null if invalid
	 */
	private static String parseUrl(String url) {
		log.fine("Parsing url.");
		
		if (url == null) {
			log.fine("url = null");
			
			return null;
		}
		
		String res = null;
		
		if (StringUtils.startsWith(url, URL_PREFIX)) {
			res = StringUtils.substring(url, URL_PREFIX.length());
		}
		
		return res;
	}
	
	@Override
	public boolean acceptsURL(String url) throws SQLException {
		boolean res;
		
		String parsedUrl = parseUrl(url);
		
		try {
			Paths.get(parsedUrl);
			res = true;
		} catch (InvalidPathException | NullPointerException e) {
			res = false;
		}
		
		log.fine("acceptsURL:" + parsedUrl + " = " + res);
		
		return res;
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		log.info("Connecting to databases started.");
		
		Switcher s;
		url = parseUrl(url);
		
		if (url == null && info == null) {
			log.fine("url and properties null => calling Switcher s = Loader.loadData()");
			
			s = Loader.loadData();
		} else if (url == null) {
			log.fine("url is null => calling Switcher s = Loader.loadData(info); (info are properties)");
			
			s = Loader.loadData(info);
		} else {
			log.fine("url is filled => calling Switcher s = Loader.loadData(url);");
			
			s = Loader.loadData(url);
		}
		
		
		log.info("Succesfully connected.");
		
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
		Properties properties = getPropertiesFromFile(url, info);
		int items = getNumberItems(properties);		
		
		DriverPropertyInfo[] res = new DriverPropertyInfo[2 + 6 * items];
		
		res[0] = getSinglePropertyInfo(true, "items", Integer.toString(items), "Number of connnections to databases.");
		res[1] = getSinglePropertyInfo(false, "default", properties.getProperty("default"), "Specifies primary connection by name.");
		
		for (int i = 0; i < items; i++) {
			res[i * 6 + 2] = getSinglePropertyInfo(i, true, "driver", "Specifies the class for database driver.");
			res[i * 6 + 3] = getSinglePropertyInfo(i, true, "url", "Specifies the database url. It may contains additional information such as user name or password.");
			res[i * 6 + 4] = getSinglePropertyInfo(i, true, "name", "Unique name to mark database connection.");
			res[i * 6 + 5] = getSinglePropertyInfo(i, false, "user", "Specifies the database user name.");
			res[i * 6 + 6] = getSinglePropertyInfo(i, false, "password", "Specifies the password to database.");
			res[i * 6 + 7] = getSinglePropertyInfo(i, true, "regexp", "Specifies regular expression that is going to be associated to this connection.");
		}
		
		return res;
	}
	
	private static Properties getPropertiesFromFile(String url, Properties info) {
		String parsedUrl = parseUrl(url);
		Properties properties = new Properties();
		
		if (parsedUrl != null) {
			try {
				properties.load(new FileInputStream(new File(parsedUrl)));
			} catch (IOException e) {
				//leave properties to be empty
				log.warning("Unable to read properties with given url: " + url);
			}
		} else if (info != null) {
			properties = new Properties(info);
		}
		
		return properties;
	}
	
	private static int getNumberItems(Properties info) {
		int items = 2;
		
		try {				
			items = Integer.parseInt(info.getProperty("items"));
			
			if (items <= 0) {
				// Number is lesser or equal to zero. Setting default value: 2
				items = 2;
			}
			
		} catch (NumberFormatException | NullPointerException e) {
			//leave default value
		}
		return items;
	}
	
	private static DriverPropertyInfo getSinglePropertyInfo(boolean required, String name, String value, String description) {
		DriverPropertyInfo info = new DriverPropertyInfo(name, emptyIfNull(value));
		info.required = required;
		info.description = description;
		
		return info;
	}
	
	private static DriverPropertyInfo getSinglePropertyInfo(int i, boolean required, String name, String description) {
		String infoName = new StringBuilder("db").append(i).append("_").append(name).toString();
		DriverPropertyInfo info = new DriverPropertyInfo(infoName, emptyIfNull(infoName));
		info.required = required;
		info.description = description;
		
		return info;
	}
	
	/**
	 * Returns the same string, but when is null, it returns empty String
	 * 
	 * @param str
	 * @return same string
	 */
	private static String emptyIfNull(String str) {
		return (str == null) ? new String() : str;
	}
	@Override
	public boolean jdbcCompliant() {
		return false;
	}
}
