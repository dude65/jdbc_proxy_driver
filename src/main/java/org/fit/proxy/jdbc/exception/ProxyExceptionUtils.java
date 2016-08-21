package org.fit.proxy.jdbc.exception;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fit.proxy.jdbc.ProxyDriver;
import org.fit.proxy.jdbc.configuration.ProxyConfiguration;

/**
 * This class provides static methods that handles with proxy exception
 * 
 * @author Ondřej Marek
 *
 */
public class ProxyExceptionUtils {
	private final static Logger log = Logger.getLogger(ProxyDriver.class.getName());
	
	/**
	 * Throws a proxy exception in case that exception is throwable
	 * @param exception proxy exception
	 * @throws ProxyException possible result
	 */
	public static void throwIfPossible(ProxyException exception) throws ProxyException {
		if (exception.isThrowable()) {
			throw exception;
		}
	}
	
	/**
	 * Throws proxy exception as sql exception in case that exception is throwable
	 * @param exception proxy exception
	 * @throws SQLException possible result
	 */
	public static void throwAsSqlIfPossible(ProxyException exception) throws SQLException {
		if (exception.isThrowable()) {
			String message = new StringBuilder(ProxyConfiguration.getCurrentParsedDate()).append(": ").append(exception.getMessage()).toString();
			throw new SQLException(message, exception);
		}
	}
	
	/**
	 * Logs all partial exceptions and throws a proxy exception in case that the exception is throwable
	 * @param exception proxy exception
	 * @param level level of logging
	 * @throws ProxyException possible result
	 */
	public static void throwAndLogAsProxy(ProxyException exception, Level level) throws ProxyException {
		logExceptions(exception, level);
		throwIfPossible(exception);
	}
	
	/**
	 * Logs all partial exceptions and throws a sql exception in case that the exception is throwable
	 * @param exception proxy exception
	 * @param level level of logging
	 * @throws SQLException possible result
	 */
	public static void throwAndLogAsSql(ProxyException exception, Level level) throws SQLException {
		logExceptions(exception, level);
		throwAsSqlIfPossible(exception);
	}
	
	/**
	 * Logs all exceptions contained in proxy exception.
	 * @param proxyException proxy exceptions
	 * @param level level of logging
	 */
	public static void logExceptions(ProxyException proxyException, Level level) {
		List<ExceptionUnit> exceptionList = proxyException.getExceptions();
		Throwable cause = proxyException.getCause();
		
		if (cause != null) {
			log.log(level, "Throwable cause:", cause);
		}
		
		for (ExceptionUnit exceptionUnit : exceptionList) {
			StringBuilder messageBuilder = new StringBuilder(ProxyConfiguration.getParsedDate(exceptionUnit.getDate())).append(": ").append(exceptionUnit.getMessage());
			String message = messageBuilder.toString();
			Exception exception = exceptionUnit.getException();
			
			if (exception == null) {
				log.log(level, message);
			} else {
				log.log(level, message, exception);
			}
		}
	}
}
