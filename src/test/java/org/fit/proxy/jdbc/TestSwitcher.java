package org.fit.proxy.jdbc;

import static org.junit.Assert.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Properties;

import org.fit.proxy.jdbc.Loader;
import org.fit.proxy.jdbc.Switcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class TestSwitcher {
	Switcher s;
	
	@Before
	public void load() throws SQLException, IOException, URISyntaxException {		
		Properties p = new TestUtils().load();
		
		s = Loader.loadData(p);
	}
	
	
	@Test
	public void test1() throws SQLException {
		ConnectionUnit fromQuery = s.getConnection("SELECT * FROM persons");
		ConnectionUnit fromName = s.getConnectionByName("database1");
		Assert.assertEquals(fromName, fromQuery);
	}
	
	@Test(expected=SQLException.class)
	public void test2() throws SQLException {
		s.unsetDefaultDatabase();
		s.getConnection(" SELECT * FROM persons");
		
		fail("There is no database that is assosiated to that query");
	}
	
	@Test
	public void test3() throws SQLException {
		s.unsetDefaultDatabase();
		ConnectionUnit fromName = s.getConnectionByName("database3");
		ConnectionUnit fromQuery = s.getConnection("INSERT INTO `homes` (`ID`, `street`, `city`, `houseNumber`, `zipCode`) VALUES (4, 'Catlover's', 'London', 8, 11111);");
		Assert.assertEquals(fromName, fromQuery);
	}
	
	@Test
	public void test4() throws SQLException {
		s.unsetDefaultDatabase();
		ConnectionUnit fromName = s.getConnection("UPDATE `homes` SET `city` = 'Madrid` WHERE `ID` = 1");
		ConnectionUnit fromQuery = s.getConnectionByName("database2");
		Assert.assertEquals(fromName, fromQuery);
	}
	
	@Test(expected=SQLException.class)
	public void test5 () throws SQLException {
		s.unsetDefaultDatabase();
		s.getConnection("eg wesw");
		
		fail("There is no database that is assosiated to that query");
	}
	
	@Test(expected=SQLException.class)
	public void test6() throws SQLException {
		s.setDefaultDatabase("wGARREG");
		
		fail("A database of that name does not exists.");
	}
	
	
	
	@Test
	public void test7() {
		try {
			s.setDefaultDatabase("database2");
			Assert.assertTrue(true);
		} catch (SQLException e) {
			fail("A database of that name exists so it should be choosen.");
		}
	}
	
	@Test
	public void test8() throws SQLException {
		s.setDefaultDatabase("database2");
		ConnectionUnit fromName = s.getConnectionByName("database2");
		ConnectionUnit fromQuery = s.getConnection("eg wesw");
		Assert.assertEquals(fromName, fromQuery);
	}
	
	@Test
	public void test9() throws SQLException {
		s.setDefaultDatabase("database2");
		ConnectionUnit fromName = s.getConnectionByName("database2");
		ConnectionUnit fromQuery = s.getConnection(" SELECT * FROM persons");
		Assert.assertEquals(fromName, fromQuery);
	}
	
	@Test(expected=SQLException.class)
	public void test10() throws SQLException {
		s.unsetDefaultDatabase();
		s.getConnection(" SELECT * FROM persons");
		
		fail("There is no database that is assosiated to that query");
	}
	
	@After
	public void close() throws SQLException {
		TestUtils.closeConnections(s.getConnectionList());
	}

}
