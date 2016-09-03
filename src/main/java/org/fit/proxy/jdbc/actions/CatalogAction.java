package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.configuration.ProxyConstants;

public class CatalogAction implements IAction {
	private final String catalog;
	public CatalogAction(String catalog) {
		this.catalog = catalog;
	}
	
	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {		
		connection.getConnection().setCatalog(catalog);
	}

	@Override
	public void runReverseAction(ConnectionUnit connection, Object value) throws SQLException {
		connection.getConnection().setCatalog((String) value);
	}

	@Override
	public String getOkMessage() {
		return new StringBuilder("Proxy connection catalog ").append(catalog).append(" was set successfully.").toString();
	}

	@Override
	public String getErrMessage() {
		return new StringBuilder("Unable to set catalog to value ").append(catalog).toString();
	}

	@Override
	public String getPropertyName() {
		return ProxyConstants.CATALOG_ACTION;
	}

	@Override
	public Object getPropertyValue() {
		return catalog;
	}

	@Override
	public Object getSaveValue(ConnectionUnit connection) throws SQLException {
		return connection.getConnection().getCatalog();
	}

	@Override
	public Object getResult() {
		return null;
	}

}
