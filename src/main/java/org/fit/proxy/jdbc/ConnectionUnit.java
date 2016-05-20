package org.fit.proxy.jdbc;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
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
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	
	private Connection connection;
	private Pattern pattern;
	private String name;
	
	public ConnectionUnit(String name, String regexp, Connection connection) {
		this.name = name;
		this.connection = connection;
		pattern = Pattern.compile(regexp);
		
		log.log(Level.INFO, "Connection unit " + name + " set up.");
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
		boolean res = m.find();
		
		log.log(Level.FINE, "Matching query (" + regexp + ") to " + name + " = " + res);
		
		return res;
	}
	
	@Override
	public String toString() {
		return name + ", " + pattern;
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
		return new EqualsBuilder().append(name, oth.name).append(pattern.toString(), oth.pattern.toString()).isEquals();
	}
	
	
	
	
}
