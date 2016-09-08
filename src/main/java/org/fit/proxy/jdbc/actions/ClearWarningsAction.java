package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

import org.fit.proxy.jdbc.ConnectionUnit;

public class ClearWarningsAction implements ISimpleAction {

	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {
		connection.getConnection().clearWarnings();
	}

	@Override
	public String getOkMessage() {
		return "Warnings cleared successfully.";
	}

	@Override
	public String getErrMessage() {
		return "Some warnings could not be cleared. Clearing unsuccessfull.";
	}
}
