package org.fit.proxy.jdbc.exception;

import java.util.LinkedList;
import java.util.List;

/**
 * Proxy exception is contained of list of other exceptions
 * 
 * @author Ondřej Marek
 *
 */
public class ProxyException extends Exception {
	private static final long serialVersionUID = 6458125935055620145L;
	private final String message;
	private Throwable cause;
	
	public ProxyException(String message) {
		this.message = message;
	}
	
	private List<ExceptionUnit> exceptionList = new LinkedList<>();
	
	/**
	 * add a new exception to the list
	 * @param exception exception
	 */
	public void addException(ExceptionUnit exception) {
		exceptionList.add(exception);
	}
	
	/**
	 * add a new exception to the list
	 * @param message message of the exception
	 */
	public void addException(String message) {
		exceptionList.add(new ExceptionUnit(message, null));
	}
	
	/**
	 * add a new exception to the list
	 * @param message message of the exception
	 * @param exception cause
	 */
	public void addException(String message, Exception exception) {
		exceptionList.add(new ExceptionUnit(message, exception));
	}
	
	/**
	 * Returns true if there are some exceptions in the list
	 * @return is throwable
	 */
	public boolean isThrowable() {
		return !exceptionList.isEmpty() || cause != null;
	}
	
	/**
	 * Returns copy of the exception list
	 * @return exception list
	 */
	public List<ExceptionUnit> getExceptions() {
		return new LinkedList<>(exceptionList);
	}
	
	@Override
	public String getMessage() {
		return message;
	}
	
	@Override
	public synchronized Throwable getCause() {
		return cause;
	}
	
	public void setCause(Throwable cause) {
		this.cause = cause;
	}

}
