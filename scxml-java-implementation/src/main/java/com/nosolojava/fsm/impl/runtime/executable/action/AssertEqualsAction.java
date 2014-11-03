package com.nosolojava.fsm.impl.runtime.executable.action;

import junit.framework.AssertionFailedError;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.CustomAction;

public class AssertEqualsAction implements CustomAction {
	private static final long serialVersionUID = -3327857193703017994L;

	private final String expectedValueExpr;
	private final String currentValueExpr;

	public AssertEqualsAction(String expectedValueExpr, String currentValueExpr) {
		super();
		this.expectedValueExpr = expectedValueExpr;
		this.currentValueExpr = currentValueExpr;
	}

	@Override
	public void run(Context context) {

		Object expectedValue = context
				.getDataByExpression(this.expectedValueExpr);
		Object currentValue = context
				.getDataByExpression(this.currentValueExpr);

		// assert ok if both are the same
		boolean conditionOK = currentValue == expectedValue;

		// assert ok if expected is not null and equals to current (or both are
		// the same)
		conditionOK = conditionOK
				|| (expectedValue != null && expectedValue.equals(currentValue));

		if (!conditionOK) {
			throw new AssertionFailedError(
					String.format(
							"Equals assertion failed, expected  %s (%s), found %s (%s)",
							expectedValue, this.expectedValueExpr,
							currentValue, this.currentValueExpr));
		}

	}

}
