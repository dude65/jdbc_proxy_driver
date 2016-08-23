package org.fit.proxy.jdbc.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.Switcher;
import org.fit.proxy.jdbc.configuration.ProxyConstants;
import org.fit.proxy.jdbc.exception.ProxyException;
import org.fit.proxy.jdbc.exception.ProxyExceptionUtils;

public class CatalogAction implements IAction {
	private final Switcher switcher;
	private final String catalog;
	private final Map<ConnectionUnit, String> save = new HashMap<>();
	
	public CatalogAction(Switcher switcher, String catalog) {
		this.switcher = switcher;
		this.catalog = catalog;
	}
	
	@Override
	public void runAction() throws SQLException {
		List<ConnectionUnit> connections = switcher.getConnectionList();
		
		for (ConnectionUnit u : connections) {
			Connection connection = u.getConnection();
			
			save.put(u, connection.getCatalog());
			connection.setCatalog(catalog);
		}
	}

	@Override
	public void runReverseAction() throws ProxyException {
		ProxyException exception = new ProxyException("Unable to set back catalog.");
		
		for (Entry<ConnectionUnit, String> entry : save.entrySet()) {
			ConnectionUnit connection = entry.getKey();
			String saveCatalog = entry.getValue();
			
			try {
				connection.getConnection().setCatalog(saveCatalog);
			} catch (SQLException sqle) {
				StringBuilder message = new StringBuilder("Unable to set back catalog in connection ").append(connection.getName()).append(" to value: ").append(catalog);
				exception.addException(message.toString(), sqle);
			}
		}
		
		ProxyExceptionUtils.throwIfPossible(exception);
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

}
