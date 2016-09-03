package org.fit.proxy.jdbc.exception;

import java.sql.SQLException;
import java.util.Date;

import org.fit.proxy.jdbc.ConnectionUnit;

/**
 * Proxy exception is contained of list of other exceptions
 * 
 * @author Ondřej Marek
 *
 */
public class ProxyException extends SQLException {
	private static final long serialVersionUID = 6458125935055620145L;
	private final Date date = new Date(System.currentTimeMillis());
	private final ConnectionUnit failConnection;
	
	public ProxyException(String message, ConnectionUnit failConnection) {
		super(message);
		this.failConnection = failConnection;
	}
	
	public ProxyException(String message, Throwable cause, ConnectionUnit failConnection) {
		super(message, cause);
		this.failConnection = failConnection;
	}

	public ConnectionUnit getFailConnection() {
		return failConnection;
	}

	public Date getDate() {
		return date;
	}

}
