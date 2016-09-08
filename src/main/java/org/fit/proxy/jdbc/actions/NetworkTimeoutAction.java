package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;
import java.util.concurrent.Executor;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.configuration.ProxyConstants;

public class NetworkTimeoutAction implements IAction {
	private final Executor executor;
	private final int timeout;
	
	public NetworkTimeoutAction(Executor executor, int timeout) {
		this.executor = executor;
		this.timeout = timeout;
	}

	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {
		connection.getConnection().setNetworkTimeout(executor, timeout);
	}

	@Override
	public void runReverseAction(ConnectionUnit connection, Object value) throws SQLException {
		connection.getConnection().setNetworkTimeout(executor, (Integer) value);
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

	@Override
	public Object getSaveValue(ConnectionUnit connection) throws SQLException {
		return connection.getConnection().getNetworkTimeout();
	}

}
