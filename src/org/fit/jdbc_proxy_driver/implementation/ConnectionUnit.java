package org.fit.jdbc_proxy_driver.implementation;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

	@Override
	public int hashCode() {
		return new HashCodeBuilder(101, 13).append(name).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (obj == this) {
			return true;
		}
		
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		
		ConnectionUnit oth = (ConnectionUnit) obj;
		return new EqualsBuilder().append(name, oth.name).isEquals();
	}
	
	
	
	
}
