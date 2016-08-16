package org.fit.proxy.jdbc.actions.back;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.exception.ProxyEceptionUtils;
import org.fit.proxy.jdbc.exception.ProxyException;

public class ReadOnlyTakeBackAction implements ITakeBackAction {
	private final Map<ConnectionUnit, Boolean> save;
	
	public ReadOnlyTakeBackAction(Map<ConnectionUnit, Boolean> save) {
		this.save = save;
	}

	@Override
	public void run() throws ProxyException {
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
		
		ProxyEceptionUtils.throwIfPossible(exception);
	}

}
