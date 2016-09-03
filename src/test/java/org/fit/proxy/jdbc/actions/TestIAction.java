package org.fit.proxy.jdbc.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.ProxyConnectionEngine;
import org.fit.proxy.jdbc.Switcher;
import org.fit.proxy.jdbc.exception.ProxyExceptionUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test action interface.
 * @author Ondřej Marek
 *
 */
public class TestIAction {
	private static final int FAKE_CONNECTIONS = 17;
	
	private static ProxyConnectionEngine ENGINE;
	private static List<ConnectionUnit> fakeConnections = new LinkedList<>();
	
	@BeforeClass
	public static void initiateConnections() {
		Map<String, ConnectionUnit> switcherMap = new HashMap<>();
		
		for (int i = 0; i < FAKE_CONNECTIONS; i++) {
			String name = new StringBuilder("Fake_connection_").append(i).toString();
			ConnectionUnit fake = new ConnectionUnit(name, "^*$", null);
			
			fakeConnections.add(fake);
			switcherMap.put(name, fake);
		}
		
		ENGINE = new ProxyConnectionEngine(new Switcher(switcherMap, null, null));
	}

	/**
	 * Ideal state - action finishes OK.
	 * @throws Exception
	 */
	@Test
	public void runOk() throws Exception {
		DummyAction action = new DummyAction(false, false);
		Map<ConnectionUnit, Integer> initiateValues = action.initiateDefaultValues(fakeConnections);
		
		ENGINE.runAction(action);
		assertNotEquals(action.getValueMap(), initiateValues);
	}
	
	/**
	 * Action fails, but it recovers to former state
	 * @throws Exception
	 */
	@Test
	public void runFail() throws Exception {
		DummyAction action = new DummyAction(true, false);
		Map<ConnectionUnit, Integer> initiateValues = action.initiateDefaultValues(fakeConnections);
		SQLException thrown = null;
		
		try {
			ENGINE.runAction(action);
		} catch (SQLException e) {
			thrown = e;
		}
		
		assertNotNull(thrown);
		assertEquals(action.getValueMap(), initiateValues);
	}
	
	/**
	 * Action fails and recovery fails too - unknown state (the worst thing which may happen)
	 * @throws Exception
	 */
	@Test
	public void runDoubleFail() throws Exception {
		DummyAction action = new DummyAction(true, true);
		Map<ConnectionUnit, Integer> initiateValues = action.initiateDefaultValues(fakeConnections);
		SQLException thrown = null;
		
		try {
			ENGINE.runAction(action);
		} catch (SQLException e) {
			thrown = e;
			
			assertFalse(ProxyExceptionUtils.actionRevertedSuccessfully(e));
		}
		
		assertNotNull(thrown);
		assertNotEquals(action.getValueMap(), initiateValues);
	}
	
	

}
