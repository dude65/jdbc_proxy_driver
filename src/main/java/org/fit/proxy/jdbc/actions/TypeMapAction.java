package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;
import java.util.Map;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.configuration.ProxyConstants;

public class TypeMapAction implements IAction {
	private final Map<String, Class<?>> map;
	
	public TypeMapAction(Map<String, Class<?>> map) {
		this.map = map;
	}

	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {
		connection.getConnection().setTypeMap(map);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void runReverseAction(ConnectionUnit connection, Object value) throws SQLException {
		connection.getConnection().setTypeMap((Map<String, Class<?>>) value);
	}

	@Override
	public Object getSaveValue(ConnectionUnit connection) throws SQLException {
		return connection.getConnection().getTypeMap();
	}

	@Override
	public String getOkMessage() {
		return "Type map was set successfully.";
	}

	@Override
	public String getErrMessage() {
		return "Unable to set up type map";
	}

	@Override
	public String getPropertyName() {
		return ProxyConstants.TYPE_MAP_ACTION;
	}

	@Override
	public Object getPropertyValue() {
		return map;
	}

}
