package org.fit.jdbc_proxy_driver.implementation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author Ondřej Marek
 *
 * This class is responsible for loading informations about databases
 */
public class Loader {
	
	/**
	 * This method is called to obtain object Switcher with a collection of database connections.
	 * 
	 * Note: This method is not done yet, this implementation is used for testing purposes only.
	 * 
	 * @return Switcher with a collection of database connections
	 * @throws SQLException if data are not correct
	 */
	public static Switcher loadData() throws SQLException {
		Connection a = null, b = null, c = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			a = DriverManager.getConnection("jdbc:mysql://localhost/proxyDatabase1", "proxyDriver1", "heslo1");
			b = DriverManager.getConnection("jdbc:mysql://localhost/proxyDatabase2", "proxyDriver2", "heslo2");
			c = DriverManager.getConnection("jdbc:mysql://localhost/proxyDatabase3", "proxyDriver3", "heslo3");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ConnectionUnit u1 = new ConnectionUnit("database1", "^SELECT*$", a);
		ConnectionUnit u2 = new ConnectionUnit("database2", "^UPDATE*$", b);
		ConnectionUnit u3 = new ConnectionUnit("database3", "^INSERT*$", c);
		
		Map<String, ConnectionUnit> map = new HashMap<>();
		map.put("database1", u1);
		map.put("database2", u2);
		map.put("database3", u3);
		
		return new Switcher(map, null);
	}
}
