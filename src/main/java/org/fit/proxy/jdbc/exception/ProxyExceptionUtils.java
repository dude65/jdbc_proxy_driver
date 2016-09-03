package org.fit.proxy.jdbc.exception;

import java.sql.SQLException;
import java.util.Iterator;
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
	 * Logs all exceptions contained in proxy exception.
	 * @param proxyException proxy exceptions
	 * @param level level of logging
	 */
	public static void logExceptions(ProxyException proxyException, Level level) {
		Throwable cause = proxyException.getCause();
		
		if (cause != null) {
			logSingleException(proxyException, level, "Proxy exception throwable cause: ");
		}
		
		for (Iterator<Throwable> iterator = proxyException.iterator(); iterator.hasNext();) {
			Throwable exception = iterator.next();
			logSingleException(exception, level, null);
		}
	}
	
	private static void logSingleException(Throwable exception, Level level, String initMessage) {
		StringBuilder logBuilder = new StringBuilder();
		
		if (exception instanceof ProxyException) {
			ProxyException proxyException = (ProxyException) exception;
			
			logBuilder.append(ProxyConfiguration.getParsedDate(proxyException.getDate())).
				append(", connection ").append(proxyException.getFailConnection().getName()).append(": ");
		}
		
		if (initMessage != null && !initMessage.isEmpty()) {
			logBuilder.append(initMessage);
		}
		
		logBuilder.append(exception.getMessage());
		String logMessage = logBuilder.toString();
		
		Throwable cause = exception.getCause();
		
		if (cause == null) {
			log.log(level, logMessage);
		} else {
			log.log(level, logMessage, cause);
		}
	}
	
	/**
	 * Sometimes is useful to know whether failed action was reverted successfully and there are no inconsistencies in databases.
	 * @param exception sql exception (or proxy exception) thrown by proxy driver
	 * @return whether action was reverted successfully
	 */
	public static boolean actionRevertedSuccessfully(SQLException exception) {
		return !(exception instanceof ProxyException) || !exception.iterator().hasNext();
	}
}
