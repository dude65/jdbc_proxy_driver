package org.fit.proxy.jdbc.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.Switcher;
import org.fit.proxy.jdbc.configuration.ProxyConstants;
import org.fit.proxy.jdbc.exception.ProxyExceptionUtils;
import org.fit.proxy.jdbc.exception.ProxyException;

public class ReadOnlyAction implements IAction {
	private final Switcher switcher;
	private final boolean readOnly;
	private final Map<ConnectionUnit, Boolean> save = new HashMap<>();

	public ReadOnlyAction(Switcher switcher, boolean readOnly) {
		this.switcher = switcher;
		this.readOnly = readOnly;
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
		
		ProxyExceptionUtils.throwIfPossible(exception);
	}

	@Override
	public String getOkMessage() {
		return new StringBuilder("Proxy connection set read only = ").append(readOnly).toString();
	}

	@Override
	public String getErrMessage() {
		return new StringBuilder("Cannot set proxy connection to read only = ").append(readOnly).toString();
	}

	@Override
	public String getPropertyName() {
		return ProxyConstants.READ_ONLY_ACTION;
	}

	@Override
	public Object getPropertyValue() {
		return readOnly;
	}

}
