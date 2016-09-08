package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

import org.fit.proxy.jdbc.ConnectionUnit;

public class CloseConnectionAction implements ISimpleAction {

	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {
		connection.getConnection().close();
	}

	@Override
	public String getOkMessage() {
		return "Proxy connection closed successfully.";
	}

	@Override
	public String getErrMessage() {
		return "Proxy connection closed with problems.";
	}
}
