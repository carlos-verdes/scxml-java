package com.nosolojava.fsm.impl.runtime.executable.basic;

import java.util.ArrayList;
import java.util.List;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Conditional;
import com.nosolojava.fsm.runtime.executable.Executable;

public class BasicConditional implements Conditional {
	private static final long serialVersionUID = -2633061454337049104L;
	protected final String condition;
	protected final List<Executable> executables = new ArrayList<Executable>();

	public BasicConditional(String condition) {
		this.condition = condition;
	}

	public BasicConditional(String condition, List<Executable> executables) {
		this(condition);
		this.executables.addAll(executables);
	}

	protected boolean evaluateCondition(Context context) {
		return context.evaluateConditionGuardExpresion(condition);
	}

	@Override
	public void run(Context context) {
		runIf(context);
	}

	@Override
	public boolean runIf(Context context) {
		boolean result = false;
		if (evaluateCondition(context)) {
			result = true;
			executeNested(context);
		}
		return result;
	}

	private void executeNested(Context context) {
		if (executables != null) {
			for (Executable executable : this.executables) {
				executable.run(context);
			}
		}
	}

}
