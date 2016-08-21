package org.fit.proxy.jdbc;

/**
 * This class holds information about properties that are set in ProxyConnection
 * @author Ondřej Marek
 */
public class ConnectionPropertiesUnit {
	private final String name;
	private Object value;
	private boolean valueSet = false;
	
	/**
	 * Creates an instance
	 * @param name of the property
	 */
	public ConnectionPropertiesUnit(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value){
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
