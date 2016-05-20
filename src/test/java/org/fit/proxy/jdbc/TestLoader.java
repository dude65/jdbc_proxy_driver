package org.fit.proxy.jdbc;


import static org.junit.Assert.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.Loader;
import org.fit.proxy.jdbc.Switcher;
import org.junit.Assert;
import org.junit.Test;

public class TestLoader {
	@Test
	public void test1() throws URISyntaxException, IOException, SQLException, ClassNotFoundException {
		Properties p = new LoadProperties().load();
		Switcher s1 = Loader.loadData(p);
		
		String path = this.getClass().getClassLoader().getResource("config.properties").toString();
		
		URI uri = new URI(path).resolve(".");
		String dir = uri.toString();
		dir = dir.substring(5, dir.length() - 1);
		
		String url1 = "jdbc:h2:~/proxyDatabase1".replaceAll("~", dir);
		String url2 = "jdbc:h2:~/proxyDatabase2".replaceAll("~", dir);
		String url3 = "jdbc:h2:~/proxyDatabase3".replaceAll("~", dir);
		
		Class.forName("org.h2.Driver");
		Connection a = DriverManager.getConnection(url1);
		Connection b = DriverManager.getConnection(url2);
		Connection c = DriverManager.getConnection(url3);
		
		ConnectionUnit u1 = new ConnectionUnit("database1", "^SELECT*", a);
		ConnectionUnit u2 = new ConnectionUnit("database2", "^UPDATE*", b);
		ConnectionUnit u3 = new ConnectionUnit("database3", "^INSERT*", c);
		
		Map<String, ConnectionUnit> map = new HashMap<>();
		map.put("database1", u1);
		map.put("database2", u2);
		map.put("database3", u3);
		
		Switcher s2 = new Switcher(map, null, null);
		
		Assert.assertEquals(s1, s2);
		
		s1.closeConnections();
		s2.closeConnections();
	}
	
	@Test(expected=SQLException.class)
	public void test2() throws URISyntaxException, IOException, SQLException {
		Properties p = new LoadProperties().load("test2.properties");
		Loader.loadData(p);
		
		fail("Expected SQLException - switcher contains two connection units with the same name.");
	}
	
	@Test(expected=SQLException.class)
	public void test3() throws URISyntaxException, IOException, SQLException {
		Properties p = new LoadProperties().load("test3.properties");
		Loader.loadData(p);
		
		fail("Expected SQLException - db connection (database2) does not contain the key db1_regexp.");
	}
	
	@Test(expected=SQLException.class)
	public void test4() throws URISyntaxException, IOException, SQLException {
		Properties p = new LoadProperties().load("test4.properties");
		Loader.loadData(p);
		
		fail("Expected SQLException - the number of databases does not match.");
	}

}
