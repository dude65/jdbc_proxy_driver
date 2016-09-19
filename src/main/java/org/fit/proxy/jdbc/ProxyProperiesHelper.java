package org.fit.proxy.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides access connection properties unit.
 * @author Ondřej Marek
 */
public class ProxyProperiesHelper implements Iterable<ConnectionPropertiesUnit> {
	private final IConnectionEnsure connectionEnsure;
	
	public ProxyProperiesHelper(IConnectionEnsure connectionEnsure) {
		this.connectionEnsure = connectionEnsure;
	}

	/**
	 * Map, that contains properties that are set in proxy connection
	 */
	private Map<String, ConnectionPropertiesUnit> proxyProperties = new HashMap<>();
	
	/**
	 * Sets property
	 * @param name property name
	 * @param value property value
	 */
	public void setProperty(String name, Object value) {
		ConnectionPropertiesUnit property = proxyProperties.get(name);
		
		if (property == null) {
			property = new ConnectionPropertiesUnit(name);
			proxyProperties.put(name, property);
		}
		
		property.setValue(value);
		
	}
	
	/**
	 * Unsets property
	 * @param name property name
	 */
	public void unsetProperty(String name) {
		ConnectionPropertiesUnit property = proxyProperties.get(name);
		
		if (property != null) {
			property.unsetValueSet();
		}
	}
	
	/**
	 * Checks whether property is set
	 * @param name property name
	 * @return whether is property set
	 */
	public boolean isPropertySet(String name){		
		ConnectionPropertiesUnit property = proxyProperties.get(name);
		
		return property != null && property.isValueSet();
	}
	
	/**
	 * Checks if the property was even initiated
	 * @param name property name
	 * @return whether property was initiated
	 */
	public boolean isPropertyInitiated(String name) {
		return proxyProperties.containsKey(name);
	}
	
	/**
	 * Returns a value by given name
	 * @param name property name
	 * @return property value
	 * @throws SQLException if property has not been set, yet.
	 */
	public Object getPropertyValue(String name) throws SQLException {	
		connectionEnsure.ensureConnectionIsAlive();
		
		if (!isPropertySet(name)) {
			String message = new StringBuilder("Attempting to get property named ").append(name).append(" which has not been set, yet!").toString();
			
			throw new SQLException(message);
		}
		
		return proxyProperties.get(name).getValue();
	}

	@Override
	public Iterator<ConnectionPropertiesUnit> iterator() {
		return proxyProperties.values().iterator();
	}
}
