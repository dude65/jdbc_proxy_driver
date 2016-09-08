package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.configuration.ProxyConstants;

public class AutoCommitAction implements IAction {
	private final boolean autoCommit;
	
	public AutoCommitAction(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {
		connection.getConnection().setAutoCommit(autoCommit);
	}

	@Override
	public void runReverseAction(ConnectionUnit connection, Object value) throws SQLException {
		connection.getConnection().setAutoCommit((Boolean) value);
	}

	@Override
	public Object getSaveValue(ConnectionUnit connection) throws SQLException {
		return connection.getConnection().getAutoCommit();
	}
	
	@Override
	public String getOkMessage() {
		return "Auto commit was set successfully.";
	}

	@Override
	public String getErrMessage() {
		return "Unable to set up auto commit.";
	}
	
	@Override
	public String getPropertyName() {
		return ProxyConstants.AUTO_COMMIT_ACTION;
	}

	@Override
	public Object getPropertyValue() {
		return autoCommit;
	}

}
