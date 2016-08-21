package org.fit.proxy.jdbc.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.Switcher;
import org.fit.proxy.jdbc.exception.ProxyEceptionUtils;
import org.fit.proxy.jdbc.exception.ProxyException;

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
	public void runAction() throws SQLException {
		List<ConnectionUnit> connections = switcher.getConnectionList();
		
		for (ConnectionUnit u : connections) {
			Connection connection = u.getConnection();
			
			save.put(u, connection.isReadOnly());
			connection.setReadOnly(readOnly);
		}
	}

	@Override
	public void runReverseAction() throws ProxyException {
		ProxyException exception = new ProxyException("Unable to set back read only properties.");
		
		for (Entry<ConnectionUnit, Boolean> entry : save.entrySet()) {
			ConnectionUnit connection = entry.getKey();
			Boolean readOnly = entry.getValue();
			
			try {
				connection.getConnection().setReadOnly(readOnly);
			} catch (SQLException sqle) {
				StringBuilder message = new StringBuilder("Unable to set back read only in connection ").append(connection.getName()).append(" to value: ").append(readOnly);
				exception.addException(message.toString(), sqle);
			}
		}
		
		ProxyEceptionUtils.throwIfPossible(exception);
	}

}
