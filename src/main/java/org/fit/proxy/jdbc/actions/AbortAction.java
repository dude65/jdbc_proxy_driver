package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;
import java.util.concurrent.Executor;

import org.fit.proxy.jdbc.ConnectionUnit;

public class AbortAction implements ISimpleAction {
	private final Executor executor;
	
	public AbortAction(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {
		connection.getConnection().abort(executor);
	}

	@Override
	public String getOkMessage() {
		return "Successfully aborted.";
	}

	@Override
	public String getErrMessage() {
		return "Some connections could not be aborted";
	}

}
