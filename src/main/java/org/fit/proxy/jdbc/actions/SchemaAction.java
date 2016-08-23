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
import org.fit.proxy.jdbc.exception.ProxyException;
import org.fit.proxy.jdbc.exception.ProxyExceptionUtils;

public class SchemaAction implements IAction {
	private final Switcher switcher;
	private final String schema;
	private final Map<ConnectionUnit, String> save = new HashMap<>();

	public SchemaAction(Switcher switcher, String schema) {
		this.switcher = switcher;
		this.schema = schema;
	}

	@Override
	public void runAction() throws SQLException {
		List<ConnectionUnit> connections = switcher.getConnectionList();
		
		for (ConnectionUnit u : connections) {
			Connection connection = u.getConnection();
			
			save.put(u, connection.getSchema());
			connection.setSchema(schema);
		}
		
	}

	@Override
	public void runReverseAction() throws ProxyException {
		ProxyException exception = new ProxyException("Unable to set back read only properties.");
		
		for (Entry<ConnectionUnit, String> entry : save.entrySet()) {
			ConnectionUnit connection = entry.getKey();
			String saveSchema = entry.getValue();
			
			try {
				connection.getConnection().setSchema(saveSchema);
			} catch (SQLException sqle) {
				StringBuilder message = new StringBuilder("Unable to set back schema in connection ").append(connection.getName()).append(" to value: ").append(schema);
				exception.addException(message.toString(), sqle);
			}
		}
		
		ProxyExceptionUtils.throwIfPossible(exception);
		
	}

	@Override
	public String getOkMessage() {
		return new StringBuilder("Proxy connection schema ").append(schema).append(" was set successfully").toString();
	}

	@Override
	public String getErrMessage() {
		return new StringBuilder("Unable to set proxy connection schema to value ").append(schema).toString();
	}

	@Override
	public String getPropertyName() {
		return ProxyConstants.SCHEMA_ACTION;
	}

	@Override
	public Object getPropertyValue() {
		return schema;
	}

}
