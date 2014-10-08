package com.nosolojava.fsm.impl.model.basic.transition;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.model.transition.Transition;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Executable;

public class BasicTransition implements Transition {
	private static final long serialVersionUID = 3297614985076145446L;

	protected final String sourceState;
	protected final String targetState;

	private final List<Executable> executables = new ArrayList<Executable>();

	private final String eventName;
	private final String guardCondition;
	private final boolean internal;

	private static final String TRANSITION_EXPR = "Transition [{0}-->{1}]";
	private static final String TRANSITION_EXPR_EXTENDED = "Transition [{0}-->{4} event({1}) cond[{2}]/actions: {3} ]";
	private final MessageFormat transitionMF = new MessageFormat(TRANSITION_EXPR);
	private final MessageFormat transitionMFExtended = new MessageFormat(TRANSITION_EXPR_EXTENDED);
	private final String toText;

	public BasicTransition(String sourceState, String targetState) {
		this(sourceState, null, targetState, null, false);
	}

	public BasicTransition(String sourceState, String eventName, String targetState) {
		this(sourceState, eventName, targetState, null, false);
	}

	public BasicTransition(String sourceState, String eventName, String targetState, String guardCondition) {
		this(sourceState, eventName, targetState, guardCondition, false);
	}

	public BasicTransition(String sourceState, String eventName, String targetState, String guardCondition,
			boolean internal) {
		this.sourceState = sourceState;
		this.targetState = targetState;
		this.eventName = eventName;
		this.guardCondition = guardCondition;
		this.internal = internal;
		this.toText = transitionMF.format(new Object[] { this.sourceState, this.targetState });
	}

	@Override
	public String getEventName() {
		return eventName;
	}

	@Override
	public boolean passGuardCondition(Context context) {
		boolean result = false;

		// if there is a condition
		if (getGuardCondition() != null && !"".equals(getGuardCondition())) {
			// return true if condition check true
			result = context.evaluateConditionGuardExpresion(getGuardCondition());
		} else {
			// no condition so --> transition pass
			result = true;
		}

		return result;
	}

	@Override
	public boolean isInternal() {

		return this.internal;
	}

	public State getTargetState(Context context) {
		return targetState != null ? context.getState(targetState) : null;
	}

	public State getSourceState(Context context) {
		return context.getState(sourceState);
	}

	@Override
	public List<Executable> getExecutables() {
		return this.executables;
	}

	@Override
	public void addExecutable(Executable executable) {
		this.executables.add(executable);
	}

	@Override
	public void addExecutables(List<Executable> executables) {
		if (executables != null) {
			this.executables.addAll(executables);
		}
	}

	@Override
	public void clearAndSetExecutables(List<Executable> executables) {
		this.executables.clear();
		addExecutables(executables);
	}

	@Override
	public String toString() {
		return this.toText;
	}

	@Override
	public String toStringVerbose() {

		return transitionMFExtended.format(new Object[] { this.sourceState, this.eventName, "" + this.guardCondition,
				this.executables.size(), this.targetState });

	}

	public String getGuardCondition() {
		return guardCondition;
	}

	public static void main(String[] args) {
		String ENTRY_STATE_EXPR = "'in-->({0})'";
		MessageFormat entryMF = new MessageFormat(ENTRY_STATE_EXPR);

		System.out.println(entryMF.format(new Object[] { "hola" }));
	}

}
