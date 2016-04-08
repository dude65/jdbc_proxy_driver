package org.fit.jdbc_proxy_driver.implementation;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Ondřej Marek
 * 
 * This class collects data about single Connection to database.
 *
 */

public class ConnectionUnit {
	final private Connection connection;
	final private Pattern pattern;
	
	public ConnectionUnit(String regexp, Connection connection) {
		this.connection = connection;
		pattern = Pattern.compile(regexp);
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	
	/**
	 * This method returns whether the SQL query suits to the SQL query
	 * @param regexp = SQL query
	 * @return match to query
	 */
	public boolean matches(String regexp) {
		Matcher m = pattern.matcher(regexp);
		return m.find();
	}
}
