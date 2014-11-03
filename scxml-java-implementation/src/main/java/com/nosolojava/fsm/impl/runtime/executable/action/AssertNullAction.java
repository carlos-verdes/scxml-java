package com.nosolojava.fsm.impl.runtime.executable.action;

import junit.framework.AssertionFailedError;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.CustomAction;

public class AssertNullAction implements CustomAction {
	private static final long serialVersionUID = -3327857193703017994L;

	private final String currentValueExpr;

	public AssertNullAction(String currentValueExpr) {
		super();
		this.currentValueExpr = currentValueExpr;
	}

	@Override
	public void run(Context context) {

		Object currentValue = context
				.getDataByExpression(this.currentValueExpr);

		// assert if is null
		boolean conditionOK = currentValue==null;

		if (!conditionOK) {
			throw new AssertionFailedError(
					String.format(
							"Null assertion failed, found %s (%s)",
							currentValue, this.currentValueExpr));
		}

	}

}
