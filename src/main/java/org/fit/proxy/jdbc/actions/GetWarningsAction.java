package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;
import java.sql.SQLWarning;

import org.fit.proxy.jdbc.ConnectionUnit;

public class GetWarningsAction implements ISimpleAction {
	private SQLWarning resultWarning;

	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {
		appendWarnings(connection.getConnection().getWarnings());
	}
	
	private void appendWarnings(SQLWarning append) {
		if (resultWarning == null) {
			resultWarning = append;
		} else {
			resultWarning.setNextWarning(append);
		}
	}

	@Override
	public String getOkMessage() {
		return "Proxy SQL warnigs collected successfully.";
	}

	@Override
	public String getErrMessage() {
		return "Unable to collect all SQL warnings.";
	}

	@Override
	public Object getResult() {
		return resultWarning;
	}

}
