package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.fit.proxy.jdbc.ConnectionUnit;
import org.fit.proxy.jdbc.configuration.TestConstants;

/**
 * This is a dummy implementation of action interface used for test purposes.
 * Action: fills a list with random numbers
 * Reverse action: set list to former values
 * @author Ondřej Marek
 *
 */
public class DummyAction implements IAction {
	private static final int FAIL_AT = 13;
	private static final int FAIL_REVERSE_AT = 7;
	
	private final boolean failAction;
	private final boolean failReverse;
	private Map<ConnectionUnit, Integer> valueMap;
	private final Random random = new Random();
	
	private int actionCounter = 0;
	
	/**
	 * Dummy action constructor, fills list with initiate values
	 * @param failAction whether to fail an action
	 * @param failReverse whether to fail reverse action
	 */
	public DummyAction(boolean failAction, boolean failReverse) {
		this.failAction = failAction;
		this.failReverse = failReverse;
	}

	@Override
	public void runAction(ConnectionUnit connection) throws SQLException {
		if (failAction && actionCounter >= FAIL_AT) {
			actionCounter = 0;
			throw new SQLException("Dummy exception");
		}
		
		valueMap.put(connection, random.nextInt());
		actionCounter++;
	}
	
	public Map<ConnectionUnit, Integer> initiateDefaultValues(List<ConnectionUnit> connections) {
		valueMap = new HashMap<>();
		
		for (ConnectionUnit connection : connections) {
			valueMap.put(connection, random.nextInt());
		}
		
		return new HashMap<>(valueMap);
	}
	
	public Map<ConnectionUnit, Integer> getValueMap() {
		return valueMap;
	}
	
	@Override
	public void runReverseAction(ConnectionUnit connection, Object value) throws SQLException {		
		if (failReverse && actionCounter >= FAIL_REVERSE_AT) {
			actionCounter = 0;
			throw new SQLException("Dummy fail reverse.");
		}
		
		valueMap.put(connection, (Integer) value);
		actionCounter++;
	}

	@Override
	public String getOkMessage() {
		return TestConstants.DUMMY_OK;
	}

	@Override
	public String getErrMessage() {
		return TestConstants.DUMMY_FAIL;
	}

	@Override
	public String getPropertyName() {
		return TestConstants.DUMMY_ACTION;
	}

	@Override
	public Object getPropertyValue() {
		return failAction;
	}

	@Override
	public Object getSaveValue(ConnectionUnit connection) throws SQLException {
		return valueMap.get(connection);
	}

}
