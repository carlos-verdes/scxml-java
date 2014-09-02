package com.nosolojava.fsm.impl.model.basic.state.comparator;

import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.model.state.comparator.DocumentOrderComparator;

public class BasicDocumentOrderComparator implements DocumentOrderComparator {
	private static final long serialVersionUID = -686568854577013440L;

	@Override
	public int compare(State left, State right) {
			return left.getDocumentOrder() - right.getDocumentOrder();
	}

}
