package org.fit.proxy.jdbc;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ondřej Marek
 * 
 * This class represents save point to a proxy database connections. It collects save points to multiple databases.
 */
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
	
	/**
	 * Getter for a map of save points
	 * @return
	 */
	public Map<ConnectionUnit, Savepoint> getSavepoints() {
		return new HashMap<>(saveList);
	}

}
