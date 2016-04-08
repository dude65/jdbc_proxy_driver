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
	final private String name;
	
	public ConnectionUnit(String name, String regexp, Connection connection) {
		this.name = name;
		this.connection = connection;
		pattern = Pattern.compile(regexp);
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public String getName() {
		return name;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ConnectionUnit))
			return false;
		ConnectionUnit other = (ConnectionUnit) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	
	
	
}
