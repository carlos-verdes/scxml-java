package com.nosolojava.fsm.impl.runtime.basic;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.nosolojava.fsm.impl.model.basic.state.comparator.BasicEntryOrderComparator;
import com.nosolojava.fsm.impl.model.basic.state.comparator.BasicExitOrderComparator;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.model.state.comparator.EntryOrderComparator;
import com.nosolojava.fsm.model.state.comparator.ExitOrderComparator;

public class StateMachineUtils {
	public static final EntryOrderComparator entryOrderComparator = new BasicEntryOrderComparator();
	public static final ExitOrderComparator exitOrderComparator = new BasicExitOrderComparator();
	public static final MessageFormat DONE_STATE_MF = new MessageFormat("done.state.{0}");
	public static final MessageFormat STATE_NAME_RESERVED_WORD = new MessageFormat(
			"State cant have {0} name because is a reserved word.");
	public static final MessageFormat CANT_FIND_INITIAL_STATE_ERROR = new MessageFormat("Can''t find initial state {0} for state {1}.");

	public static List<State> stateToList(State state) {
		List<State> result = new ArrayList<State>();
		loadStateIntoCollection(state, result);
		return result;
	}

	public static SortedSet<State> stateToOrderedSet(State state) {
		SortedSet<State> result = new TreeSet<State>();
		loadStateIntoCollection(state, result);
		return result;
	}

	public static void loadStateIntoCollection(State state, Collection<State> stateCollection) {
		stateCollection.add(state);

		if (state.hasChildrens()) {
			for (State children : state.getChildrens()) {
				loadStateIntoCollection(children, stateCollection);
			}
		}
	}

}
