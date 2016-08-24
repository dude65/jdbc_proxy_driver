package org.fit.proxy.jdbc.actions;

import org.fit.proxy.jdbc.exception.ProxyException;

/**
 * Interface to run actions
 * @author Ondřej Marek
 *
 */
public interface IAction extends ISimpleAction {	
	/**
	 * Runs action that repairs faults caused by runAction()
	 * @throws ProxyException if something goes wrong
	 */
	public void runReverseAction() throws ProxyException;
	
	/**
	 * Returns the name of the property that is changed
	 * @return name
	 */
	public String getPropertyName();
	
	/**
	 * Returns the value of the property that is changed
	 * @return value 
	 */
	public Object getPropertyValue();
}
