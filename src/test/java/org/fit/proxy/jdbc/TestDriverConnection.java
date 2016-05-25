package org.fit.proxy.jdbc;

import static org.junit.Assert.*;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDriverConnection {
	private Connection proxy;
	private Connection h1;
	private Connection h2;
	private Connection h3;
	
	
	private ResultSet getResults(Connection conn, String table) throws Exception {
		Statement s = conn.createStatement();
		ResultSet res = s.executeQuery("SELECT * FROM " + table);
		
		return res;
	}
	
	/**
	 * This class compares two result sets if they are the same. It compares column counts and then it iterates through all results and compares them among themselves. It resolves three data types: String, Clob and Integer.
	 * @param a ResultSet
	 * @param b ResultSet
	 * @return if they are the same
	 * @throws Exception
	 */
	private boolean resultEquals(ResultSet a, ResultSet b) throws Exception {
		boolean res;
		
		ResultSetMetaData mda = a.getMetaData();
		ResultSetMetaData mdb = b.getMetaData();
		
		int columns = mda.getColumnCount();
		res = columns == mdb.getColumnCount();
		
		if (res) {
			while (a.next() && b.next() && res) {
				for (int i = 1; i <= columns && res; i++) {					
					if (mda.getColumnClassName(i).equals("java.lang.String")) {
						res = a.getString(i).equals(b.getString(i));
					} else if (mda.getColumnClassName(i).equals("java.sql.Clob")) {
						Clob aClob = a.getClob(i);
						Clob bClob = b.getClob(i);
						
						if (aClob != null && bClob != null) {
							res = aClob.toString().split(":")[1].equals(bClob.toString().split(":")[1]);
						} else {
							res = aClob == bClob;
						}
					} else {
						res = a.getInt(i) == b.getInt(i);
					}
				}
				
			}
			
			res = res && a.next() == false && b.next() == false;
		}
		
		return res;
	}
	
	@Before
	public void setUp() throws Exception {
		Properties p = new LoadProperties().load();
		
		proxy = new ProxyDriver().connect(null, p);
		
		String path = this.getClass().getClassLoader().getResource("config.properties").toString();
		
		URI uri = new URI(path).resolve(".");
		String dir = uri.toString();
		dir = dir.substring(5, dir.length() - 1);
		
		String url1 = "jdbc:h2:~/proxyDatabase1".replaceAll("~", dir);
		String url2 = "jdbc:h2:~/proxyDatabase2".replaceAll("~", dir);
		String url3 = "jdbc:h2:~/proxyDatabase3".replaceAll("~", dir);
		
		Class.forName("org.h2.Driver");
		h1 = DriverManager.getConnection(url1);
		h2 = DriverManager.getConnection(url2);
		h3 = DriverManager.getConnection(url3);
		
		String insertPath = this.getClass().getClassLoader().getResource("insert.sql").toString().substring(5);
		String script = new String(Files.readAllBytes(Paths.get(insertPath)));
		
		Statement s = h1.createStatement();
		s.execute(script);
		s.close();
		
		s = h2.createStatement();
		s.execute(script);
		s.execute("UPDATE persons set firstName = 'Ondra' WHERE ID = 1");
		s.execute("INSERT INTO `homes` (`street`, `city`, `houseNumber`, `zipCode`) VALUES ('Klášterská', 'Kladno', '7', '47129')");
		s.close();
		
		s = h3.createStatement();
		s.execute(script);
		s.execute("DELETE FROM persons WHERE ID = 5");
		s.execute("UPDATE homes set houseNumber = 1777 WHERE ID = 2");
		s.close();		
	}

	@After
	public void tearDown() throws Exception {
		proxy.close();
		h1.close();
		h2.close();
		h3.close();
	}

	@Test
	public void test1() throws Exception {
		ResultSet rs1 = getResults(h1, "persons");
		ResultSet prs = getResults(proxy, "persons");
		
		assertTrue(resultEquals(rs1, prs));
		
		rs1.close();
		prs.close();
	}
	
	@Test
	public void test2() throws Exception {
		ResultSet rs1_a = getResults(h1, "persons");
		ResultSet rs2_a = getResults(h2, "persons");
		ResultSet rs3_a = getResults(h3, "persons");
		
		Statement proxyStat = proxy.createStatement();
		proxyStat.execute("UPDATE persons SET firstName = 'Peter'");
		
		ResultSet rs1_b = getResults(h1, "persons");
		ResultSet rs2_b = getResults(h2, "persons");
		ResultSet rs3_b = getResults(h3, "persons");
		
		assertTrue(resultEquals(rs1_a, rs1_b) && !resultEquals(rs2_a, rs2_b) && resultEquals(rs3_a, rs3_b));
		
		rs1_a.close();
		rs1_b.close();
		rs2_a.close();
		rs2_b.close();
		rs3_a.close();
		rs3_b.close();
	}
	
	@Test
	public void test3() throws Exception {
		ResultSet rs1_a = getResults(h1, "homes");
		ResultSet rs2_a = getResults(h2, "homes");
		ResultSet rs3_a = getResults(h3, "homes");
		
		Statement proxyStat = proxy.createStatement();
		proxyStat.execute("INSERT INTO `homes` (`street`, `city`, `houseNumber`, `zipCode`) VALUES ('Hrachová', 'Ostrava', '11', '58174')");
		
		ResultSet rs1_b = getResults(h1, "homes");
		ResultSet rs2_b = getResults(h2, "homes");
		ResultSet rs3_b = getResults(h3, "homes");
		
		assertTrue(resultEquals(rs1_a, rs1_b) && resultEquals(rs2_a, rs2_b) && !resultEquals(rs3_a, rs3_b));
		
		rs1_a.close();
		rs1_b.close();
		rs2_a.close();
		rs2_b.close();
		rs3_a.close();
		rs3_b.close();
	}
	
	@Test
	public void test4() throws Exception {
		ResultSet rs1_a = getResults(h1, "homes");
		ResultSet rs2_a = getResults(h2, "homes");
		ResultSet rs3_a = getResults(h3, "homes");
		
		Statement proxyStat = proxy.createStatement();
		proxyStat.execute("INSERT INTO `homes` (`street`, `city`, `houseNumber`, `zipCode`) VALUES ('Hrachová', 'Ostrava', '11', '58174')");
		proxyStat.execute("UPDATE homes SET street = 'Kancelářská' WHERE ID = 3");
		
		ResultSet rs1_b = getResults(h1, "homes");
		ResultSet rs2_b = getResults(h2, "homes");
		ResultSet rs3_b = getResults(h3, "homes");
		
		assertTrue(resultEquals(rs1_a, rs1_b) && !resultEquals(rs2_a, rs2_b) && !resultEquals(rs3_a, rs3_b));
		
		rs1_a.close();
		rs1_b.close();
		rs2_a.close();
		rs2_b.close();
		rs3_a.close();
		rs3_b.close();
	}
	
	@Test(expected=SQLException.class)
	public void test5() throws Exception {
		Statement proxyStat = proxy.createStatement();
		
		try {
			proxyStat.execute("TRUNCATE TABLE persons");
		} catch (SQLException e) {
			proxyStat.close();
			throw e;
		}
		
		fail("Expected SQL Exception - default database is not set.");
	}

}
