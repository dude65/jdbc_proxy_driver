package org.fit.jdbc_proxy_driver.implementation;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

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

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		boolean res;
		
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
		
		if (url == null && info == null) {
			s = Loader.loadData();
		} else if (info == null) {
			s = Loader.loadData(url);
		} else {
			s = Loader.loadData(info);
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
	public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1)
			throws SQLException {
		throw new UnsupportedOperationException("Not supported yet");
	}

	@Override
	public boolean jdbcCompliant() {
		return false;
	}
}
