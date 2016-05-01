package org.fit.jdbc_proxy_driver.implementation;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Map;

public class ProxySavepoint implements Savepoint{
	private static int currID = 0;
	private int id;
	private String name;
	private Map<ConnectionUnit, Savepoint> saveList;
	
	public ProxySavepoint(String name, Map<ConnectionUnit, Savepoint> saveList) {
		id = currID++;
		
		if (name == null) {
			this.name = "db" + id + "_savepoint";
		} else {
			this.name = name;
		}
		
		this.saveList = saveList;
	}

	@Override
	public int getSavepointId() throws SQLException {
		return id;
	}

	@Override
	public String getSavepointName() throws SQLException {
		return name;
	}
	
	public Map<ConnectionUnit, Savepoint> getSavepoints() {
		return saveList;
	}

}
