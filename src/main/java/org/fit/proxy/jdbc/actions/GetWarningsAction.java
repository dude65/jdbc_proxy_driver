package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.Switcher;

public class GetWarningsAction implements ISimpleAction {
	private final Switcher switcher;
	private SQLWarning resultWarning;
	
	public GetWarningsAction(Switcher switcher, SQLWarning resultWarning) {
		this.switcher = switcher;
		this.resultWarning = resultWarning;
	}

	@Override
	public void runAction() throws SQLException {
		List<ConnectionUnit> connections = switcher.getConnectionList();
		
		for (ConnectionUnit u : connections) {
			appendWarnings(u.getConnection().getWarnings());
		}
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
		return "An error occured in connection ${connection} when collecting proxy SQL warnings";
	}

}
