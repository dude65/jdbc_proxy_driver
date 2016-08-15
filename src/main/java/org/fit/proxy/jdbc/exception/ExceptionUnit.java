package org.fit.proxy.jdbc.exception;

import java.util.Date;

/**
 * This class collects data about exception
 * 
 * @author Ondřej Marek
 *
 */
public class ExceptionUnit {
	private final Date date = new Date(System.currentTimeMillis());
	private final String message;
	private final Exception exception;
	
	/**
	 * Constructor of the message
	 * @param message message
	 * @param exception cause
	 */
	public ExceptionUnit(String message, Exception exception) {
		this.message = message;
		this.exception = exception;
		
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getMessage() {
		return message;
	}

	public Exception getException() {
		return exception;
	}
}
