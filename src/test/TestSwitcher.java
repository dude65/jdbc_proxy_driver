package test;

import java.sql.SQLException;

import org.fit.jdbc_proxy_driver.implementation.Loader;
import org.fit.jdbc_proxy_driver.implementation.Switcher;
import org.junit.Test;

public class TestSwitcher {
	Switcher s;
	public TestSwitcher() throws SQLException {
		s = Loader.loadData();
	}
	
	
	@Test
	public void test1() throws SQLException {
		assert(s.getConnection("SELECT * FROM persons") == s.getConnectionByName("database1"));
	}
	
	@Test
	public void test2() throws SQLException {
		assert(s.getConnection(" SELECT * FROM persons") == null);
	}
	
	@Test
	public void test3() throws SQLException {
		assert(s.getConnection("INSERT INTO `homes` (`ID`, `street`, `city`, `houseNumber`, `zipCode`) VALUES (4, 'Catlover's', 'London', 8, 11111);") == s.getConnectionByName("database3"));
	}
	
	@Test
	public void test4() throws SQLException {
		assert(s.getConnection("UPDATE `homes` SET `city` = 'Madrid` WHERE `ID` = 1") == s.getConnectionByName("database2"));
	}
	
	@Test
	public void test5 () throws SQLException {
		assert(s.getConnection("eg wesw") == null);
	}
	
	@Test
	public void test6() {
		try {
			s.setDefaultDatabase("wGARREG");
			assert(false);
		} catch (SQLException e) {
			assert(true);
		}
	}
	
	
	
	@Test
	public void test7() {
		try {
			s.setDefaultDatabase("database2");
			assert(true);
		} catch (SQLException e) {
			assert(false);
		}
	}
	
	@Test
	public void test8() throws SQLException {
		assert(s.getConnection("eg wesw") == s.getConnectionByName("database2"));
	}
	
	@Test
	public void test9() throws SQLException {
		assert(s.getConnection(" SELECT * FROM persons") == s.getConnectionByName("database2"));
	}

}
