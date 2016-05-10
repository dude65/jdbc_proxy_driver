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
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		int items = 2;
		
		if (info != null && info.containsKey("items")) {
			try {
				items = Integer.parseInt(info.getProperty("items"));
				
				if (items <= 0) {
					items = 2;
				}
			} catch (NumberFormatException e) {
			}
		}
		
		DriverPropertyInfo[] res = new DriverPropertyInfo[2 + 6 * items];
		
		res[0] = new DriverPropertyInfo("items", "Number of connnections to databases.");
		res[1] = new DriverPropertyInfo("default", "Specifies primary connection by name.");
		
		res[0].required = true;
		res[1].required = false;
		
		for (int i = 0; i < items; i++) {
			res[i * 6 + 2] = new DriverPropertyInfo("db" + i + "_driver", "Specifies the class for database driver.");
			res[i * 6 + 2].required = true;
			
			res[i * 6 + 3] = new DriverPropertyInfo("db" + i + "_url", "Specifies the database url. It may contains additional information such as user name or password.");
			res[i * 6 + 3].required = true;
			
			res[i * 6 + 4] = new DriverPropertyInfo("db" + i + "_name", "Unique name to mark database connection.");
			res[i * 6 + 4].required = true;
			
			res[i * 6 + 5] = new DriverPropertyInfo("db" + i + "_user", "Specifies the database user name.");
			res[i * 6 + 5].required = false;
			
			res[i * 6 + 6] = new DriverPropertyInfo("db" + i + "_password", "Specifies the password to database.");
			res[i * 6 + 6].required = false;
			
			res[i * 6 + 7] = new DriverPropertyInfo("db" + i + "_regexp", "Specifies regular expression that is going to be associated to this connection.");
			res[i * 6 + 7].required = true;
		}
		
		
		return res;
	}
	@Override
	public boolean jdbcCompliant() {
		return false;
	}
}
