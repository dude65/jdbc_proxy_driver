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
	
	/**
	 * It parses given URL with prefix and returns the path to properties file
	 * 
	 * @param url with prefix
	 * @return path to properties file
	 * @throws SQLException
	 */
	private static String parseUrl(String url) throws SQLException {
		if (url == null) {
			return null;
		}
		
		String res = null;
		String prefix = "jdbc:proxy:";
		
		if (StringUtils.startsWith(url, prefix)) {
			res = StringUtils.substring(url, prefix.length());
		}
		
		return res;
	}
	
	@Override
	public boolean acceptsURL(String url) throws SQLException {
		boolean res;
		
		url = parseUrl(url);
		
		try {
			Paths.get(url);
			res = true;
		} catch (InvalidPathException | NullPointerException e) {
			res = false;
		}
		
		return res;
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		Switcher s;
		url = parseUrl(url);
		
		if (url == null && info == null) {
			s = Loader.loadData();
		} else if (url == null) {
			s = Loader.loadData(info);
		} else {
			s = Loader.loadData(url);
		}
		
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
		throw new UnsupportedOperationException("Not supported yet");
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		int items = 2;
		
		url = parseUrl(url);
		
		if (info == null) {
			info = new Properties();
		}
		
		if (url != null) {
			try {
				info.clear();
				info.load(new FileInputStream(url));
			} catch (IOException e) {
				throw new SQLException("Cannot load properties file from given url.");
			}
		}
		
		
		if (info.containsKey("items")) {
			try {
				items = Integer.parseInt(info.getProperty("items"));
				
				if (items <= 0) {
					items = 2;
				}
			} catch (NumberFormatException e) {
			}
		}
		
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
		}
		
		return str;
	}
	@Override
	public boolean jdbcCompliant() {
		return false;
	}
}
