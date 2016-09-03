package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.configuration.ProxyConstants;

public class ReadOnlyAction implements IAction {
	private final boolean readOnly;

	public ReadOnlyAction(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {
		connection.getConnection().setReadOnly(readOnly);
	}

	@Override
	public void runReverseAction(ConnectionUnit connection, Object value) throws SQLException {
		connection.getConnection().setReadOnly((Boolean) value);
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

	@Override
	public Object getSaveValue(ConnectionUnit connection) throws SQLException {
		return connection.getConnection().isReadOnly();
	}

	@Override
	public Object getResult() {
		return null;
	}

}
