package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.configuration.ProxyConstants;

public class SchemaAction implements IAction {
	private final String schema;

	public SchemaAction(String schema) {
		this.schema = schema;
	}

	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {
		connection.getConnection().setSchema(schema);
	}

	@Override
	public void runReverseAction(ConnectionUnit connection, Object value) throws SQLException {
		connection.getConnection().setSchema((String) value);
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

	@Override
	public Object getSaveValue(ConnectionUnit connection) throws SQLException {
		return connection.getConnection().getSchema();
	}
}
