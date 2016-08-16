package org.fit.proxy.jdbc.actions.back;

import org.fit.proxy.jdbc.exception.ProxyException;

public interface ITakeBackAction {

	/**
	 * Runs action
	 * 
	 * @throws ProxyException if something goes wrong 
	 */
	public void run() throws ProxyException;
}
