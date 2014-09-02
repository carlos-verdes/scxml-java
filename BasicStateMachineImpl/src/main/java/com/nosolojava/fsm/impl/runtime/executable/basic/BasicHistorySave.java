package com.nosolojava.fsm.impl.runtime.executable.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;

import com.nosolojava.fsm.model.state.HistoryState;
import com.nosolojava.fsm.model.state.HistoryTypes;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.Executable;

public class BasicHistorySave implements Executable {
	private static final long serialVersionUID = -915001182581264297L;

	private final String stateName;
	private final HistoryState historyState;
	private final String savingStatesName;

	public BasicHistorySave(String stateName, HistoryState historyState) {
		super();
		this.stateName = stateName;
		this.historyState = historyState;
		this.savingStatesName = stateName+"_"+historyState.getId();
	}

	@Override
	public void run(Context context) {
		State state = context.getState(stateName);

		SortedSet<State> activeStates = context.getActiveStates();

		Collection<State> activeChildrens = new ArrayList<State>();
		if (HistoryTypes.SHALLOW.equals(this.historyState.getType())) {
			for (State children : state.getChildrens()) {
				if (activeStates.contains(children)) {
					activeChildrens.add(children);
					break;
				}
			}

		} else {
			activeChildrens = calculateActiveChildrensRec(state, activeStates, activeChildrens);
		}

		context.createVarIfDontExist(savingStatesName, activeChildrens);
	}

	private Collection<State> calculateActiveChildrensRec(State state, SortedSet<State> activeStates,
			Collection<State> activeChildrens) {
		for (State children : state.getChildrens()) {
			if (activeStates.contains(children)) {
				activeChildrens.add(children);
				if (children.hasChildrens()) {
					activeChildrens = calculateActiveChildrensRec(children, activeStates, activeChildrens);
				}
				break;
			}
		}

		return activeChildrens;
	}

}
