package com.nosolojava.fsm.impl.model.basic.state.comparator;

import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.model.state.comparator.EntryOrderComparator;

public class BasicEntryOrderComparator implements EntryOrderComparator {

	@Override
	public int compare(State left, State right) {

		int result = left.getDocumentOrder() - right.getDocumentOrder();
		return result;
	}

}
