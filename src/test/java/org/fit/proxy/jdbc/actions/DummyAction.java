package org.fit.proxy.jdbc.actions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.fit.proxy.jdbc.configuration.TestConstants;
import org.fit.proxy.jdbc.exception.ProxyException;

/**
 * This is a dummy implementation of action interface used for test purposes.
 * Action: fills a list with random numbers
 * Reverse action: set list to former values
 * @author Ondřej Marek
 *
 */
public class DummyAction implements IAction {
	private static final int COUNT_TO = 17;
	private static final int FAIL_AT = 13;
	private static final int FAIL_REVERSE_AT = 7;
	
	private final boolean failAction;
	private final boolean failReverse;
	private final List<Integer> testList = new ArrayList<>(COUNT_TO);
	private final List<Integer> saveList = new ArrayList<>(COUNT_TO);
	
	/**
	 * Dummy action constructor, fills list with initiate values
	 * @param failAction whether to fail an action
	 * @param failReverse whether to fail reverse action
	 */
	public DummyAction(boolean failAction, boolean failReverse) {
		this.failAction = failAction;
		this.failReverse = failReverse;
		
		Random random = new Random();
		
		for (int i = 0; i < COUNT_TO; i++) {
			Integer randInt = random.nextInt();
			saveList.add(i, randInt);
			testList.add(i, randInt);		
		}
	}

	@Override
	public void runAction() throws SQLException {
		Random random = new Random();
		
		for (int i = 0; i < COUNT_TO; i++) {
			Integer randInt = random.nextInt();
			
			if (failAction && i >= FAIL_AT) {
				throw new SQLException("Dummy exception");
			}
			
			testList.set(i, randInt);			
		}
	}
	
	@Override
	public void runReverseAction() throws ProxyException {		
		for (int i = 0; i < saveList.size(); i++) {
			if (failReverse && i >= FAIL_REVERSE_AT) {
				throw new ProxyException("Dummy fail reverse.");
			}
			
			testList.set(i, saveList.get(i));
		}
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

	public List<Integer> getTestList() {
		return testList;
	}

}
