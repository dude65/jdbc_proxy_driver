package org.fit.proxy.jdbc.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.Switcher;
import org.fit.proxy.jdbc.configuration.ProxyConstants;
import org.fit.proxy.jdbc.exception.ProxyException;
import org.fit.proxy.jdbc.exception.ProxyExceptionUtils;

public class NetworkTimeoutAction implements IAction {
	private final Switcher switcher;
	private final Executor executor;
	private final int timeout;
	private final Map<ConnectionUnit, Integer> save = new HashMap<>();
	
	public NetworkTimeoutAction(Switcher switcher, Executor executor, int timeout) {
		this.switcher = switcher;
		this.executor = executor;
		this.timeout = timeout;
	}

	@Override
	public void runAction() throws SQLException {
		List<ConnectionUnit> connections = switcher.getConnectionList();
		
		for (ConnectionUnit u : connections) {
			Connection connection = u.getConnection();
			
			save.put(u, connection.getNetworkTimeout());
			connection.setNetworkTimeout(executor, timeout);
		}
	}

	@Override
	public void runReverseAction() throws ProxyException {
		ProxyException exception = new ProxyException("Unable to set back network timeout.");
		
		for (Entry<ConnectionUnit, Integer> entry : save.entrySet()) {
			ConnectionUnit connection = entry.getKey();
			int saveTimeout = entry.getValue();
			
			try {
				connection.getConnection().setNetworkTimeout(executor, saveTimeout);
			} catch (SQLException sqle) {
				StringBuilder message = new StringBuilder("Unable to set back network timeout in connection ").append(connection.getName()).append(" to value: ").append(timeout);
				exception.addException(message.toString(), sqle);
			}
		}
		
		ProxyExceptionUtils.throwIfPossible(exception);
	}

	@Override
	public String getOkMessage() {
		return new StringBuilder("Proxy connection network timeout set to value ").append(timeout).append(" miliseconds successfully.").toString();
	}

	@Override
	public String getErrMessage() {
		return new StringBuilder("Unable to set network timeout to value ").append(timeout).append(" miliseconds.").toString();
	}

	@Override
	public String getPropertyName() {
		return ProxyConstants.NETWORK_TIMEOUT_ACTION;
	}

	@Override
	public Object getPropertyValue() {
		return timeout;
	}

}
