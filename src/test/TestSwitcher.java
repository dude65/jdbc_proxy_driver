package test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;

import org.fit.jdbc_proxy_driver.implementation.Loader;
import org.fit.jdbc_proxy_driver.implementation.Switcher;
import org.h2.tools.DeleteDbFiles;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class TestSwitcher {
	Switcher s;
	
	@Before
	public void load() throws SQLException, IOException, URISyntaxException {
		
		
		String path = this.getClass().getClassLoader().getResource("config.properties").toString();
		
		URI uri = new URI(path).resolve(".");
		String dir = uri.toString();
		dir = dir.substring(5, dir.length() - 1);
		
		DeleteDbFiles.execute(dir, "proxyDatabase1", true);
		DeleteDbFiles.execute(dir, "proxyDatabase2", true);
		DeleteDbFiles.execute(dir, "proxyDatabase3", true);
		
		byte [] propBytes = Files.readAllBytes(Paths.get(new URI(path)));
		
		String info = new String(propBytes).replaceAll("~", dir);
		
		InputStream is = new ByteArrayInputStream(info.getBytes());
		Properties p = new Properties();
		p.load(is);
		
		
		s = Loader.loadData(p);
	}
	
	
	@Test
	public void test1() throws SQLException {
		Assert.assertEquals(s.getConnection("SELECT * FROM persons"), s.getConnectionByName("database1"));
	}
	
	@Test(expected=SQLException.class)
	public void test2() throws SQLException {
		s.unsetDefaultDatabase();
		s.getConnection(" SELECT * FROM persons");
	}
	
	@Test
	public void test3() throws SQLException {
		s.unsetDefaultDatabase();
		Assert.assertEquals(s.getConnection("INSERT INTO `homes` (`ID`, `street`, `city`, `houseNumber`, `zipCode`) VALUES (4, 'Catlover's', 'London', 8, 11111);"), s.getConnectionByName("database3"));
	}
	
	@Test
	public void test4() throws SQLException {
		s.unsetDefaultDatabase();
		Assert.assertEquals(s.getConnection("UPDATE `homes` SET `city` = 'Madrid` WHERE `ID` = 1"), s.getConnectionByName("database2"));
	}
	
	@Test(expected=SQLException.class)
	public void test5 () throws SQLException {
		s.unsetDefaultDatabase();
		s.getConnection("eg wesw");
	}
	
	@Test(expected=SQLException.class)
	public void test6() throws SQLException {
		s.setDefaultDatabase("wGARREG");
	}
	
	
	
	@Test
	public void test7() {
		try {
			s.setDefaultDatabase("database2");
			Assert.assertTrue(true);
		} catch (SQLException e) {
			Assert.assertTrue(false);
		}
	}
	
	@Test
	public void test8() throws SQLException {
		s.setDefaultDatabase("database2");
		Assert.assertEquals(s.getConnection("eg wesw"), s.getConnectionByName("database2"));
	}
	
	@Test
	public void test9() throws SQLException {
		s.setDefaultDatabase("database2");
		Assert.assertEquals(s.getConnection(" SELECT * FROM persons"), s.getConnectionByName("database2"));
	}
	
	@Test(expected=SQLException.class)
	public void test10() throws SQLException {
		s.unsetDefaultDatabase();
		s.getConnection(" SELECT * FROM persons");
	}
	
	@After
	public void close() throws SQLException {
		s.closeConnections();
	}

}
