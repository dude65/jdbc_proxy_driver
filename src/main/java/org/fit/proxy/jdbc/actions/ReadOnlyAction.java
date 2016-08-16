package org.fit.proxy.jdbc.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.Switcher;

public class ReadOnlyAction implements IAction {
	private final Switcher switcher;
	private final boolean readOnly;
	private final Map<ConnectionUnit, Boolean> save;

	public ReadOnlyAction(Switcher switcher, boolean readOnly, Map<ConnectionUnit, Boolean> save) {
		this.switcher = switcher;
		this.readOnly = readOnly;
		this.save = save;
	}

	@Override
	public void run() throws SQLException {
		List<ConnectionUnit> connections = switcher.getConnectionList();
		
		for (ConnectionUnit u : connections) {
			Connection connection = u.getConnection();
			
			save.put(u, connection.isReadOnly());
			connection.setReadOnly(readOnly);
		}
	}

}
