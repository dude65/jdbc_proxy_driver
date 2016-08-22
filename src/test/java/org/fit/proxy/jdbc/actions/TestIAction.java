package org.fit.proxy.jdbc.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.fit.proxy.jdbc.ProxyConnectionEngine;
import org.fit.proxy.jdbc.exception.ProxyException;
import org.junit.Test;

/**
 * Test action interface.
 * @author Ondřej Marek
 *
 */
public class TestIAction {
	private static final ProxyConnectionEngine ENGINE = new ProxyConnectionEngine();

	/**
	 * Ideal state - action finishes OK.
	 * @throws Exception
	 */
	@Test
	public void runOk() throws Exception {
		DummyAction action = new DummyAction(false, false);
		List<Integer> testList = new ArrayList<>(action.getTestList());
		
		ENGINE.runAction(action);
		assertNotEquals(action.getTestList(), testList);
	}
	
	/**
	 * Action fails, but it recovers to former state
	 * @throws Exception
	 */
	@Test
	public void runFail() throws Exception {
		DummyAction action = new DummyAction(true, false);
		List<Integer> testList = new ArrayList<>(action.getTestList());
		boolean thrown = false;
		
		try {
			ENGINE.runAction(action);
		} catch (SQLException e) {
			thrown = true;
		}
		
		assertTrue(thrown);
		assertEquals(action.getTestList(), testList);
	}
	
	/**
	 * Action fails and recovery fails too - unknown state (the worst thing which may happen)
	 * @throws Exception
	 */
	@Test
	public void runDoubleFail() throws Exception {
		DummyAction action = new DummyAction(true, true);
		List<Integer> testList = new ArrayList<>(action.getTestList());
		boolean thrown = false;
		
		try {
			ENGINE.runAction(action);
		} catch (SQLException e) {
			thrown = true;
			
			assertTrue(e.getCause() instanceof ProxyException);
		}
		
		assertTrue(thrown);
		assertNotEquals(action.getTestList(), testList);
	}

}
