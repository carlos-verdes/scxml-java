package com.nosolojava.fsm.impl.model.basic.state.comparator;

import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.model.state.comparator.ExitOrderComparator;

public class BasicExitOrderComparator implements ExitOrderComparator {

	@Override
	public int compare(State left, State right) {
		return right.getDocumentOrder()-left.getDocumentOrder();
	}

}
