package org.fit.proxy.jdbc.configuration;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * This class contains configuration information of proxy driver.
 * 
 * @author Ondřej Marek
 *
 */
public class ProxyConfiguration {
	private static String dateFormat = "dd.MM.yyyy HH:mm:ss";

	public static String getDateFormat() {
		return dateFormat;
	}

	public static void setDateFormat(String dateFormat) {
		ProxyConfiguration.dateFormat = dateFormat;
	}
	
	/**
	 * Returns a formated current date
	 * @return formated date
	 */
	public static String getCurrentParsedDate() {
		return DateFormatUtils.format(new Date(System.currentTimeMillis()), dateFormat);
	}
	
	/**
	 * Returns a formated date
	 * @param date date to be formated
	 * @return formated date
	 */
	public static String getParsedDate(Date date) {
		return DateFormatUtils.format(date, dateFormat);
	}
}
