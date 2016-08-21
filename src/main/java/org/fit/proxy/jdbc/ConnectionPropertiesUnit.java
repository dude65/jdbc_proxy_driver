package org.fit.proxy.jdbc;

import org.fit.proxy.jdbc.exception.ProxyException;

/**
 * This class holds information about properties that are set in ProxyConnection
 * @author Ondřej Marek
 */
public class ConnectionPropertiesUnit {
	private final String name;
	private final Class<Object> valueClass;
	private Object value;
	private boolean valueSet = false;
	
	/**
	 * Creates an instance
	 * @param name of the property
	 * @param valueClass expected data type
	 */
	public ConnectionPropertiesUnit(String name, Class<Object> valueClass) {
		this.name = name;
		this.valueClass = valueClass;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value. It fails with an exception in case that data types are not compatible
	 * @param value
	 * @throws ProxyException
	 */
	public void setValue(Object value) throws ProxyException {
		if (!valueClass.isInstance(value)) {
			StringBuilder messageBuilder = new StringBuilder("Incompatible data types in proxy connection property:");
			messageBuilder.append(name).append(". Expected: ").append(valueClass.getName());
			messageBuilder.append(", provided: ").append(value.getClass().getName());
			
			throw new ProxyException(messageBuilder.toString());
		}
		
		this.value = value;
		valueSet = true;
	}

	public boolean isValueSet() {
		return valueSet;
	}

	public void unsetValueSet() {
		this.valueSet = false;
	}
	
	
}
