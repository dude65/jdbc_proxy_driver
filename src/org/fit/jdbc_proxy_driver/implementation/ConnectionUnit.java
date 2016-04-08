package org.fit.jdbc_proxy_driver.implementation;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public boolean matches(String regexp) {
		Matcher m = pattern.matcher(regexp);
		return m.find();
	}
}
