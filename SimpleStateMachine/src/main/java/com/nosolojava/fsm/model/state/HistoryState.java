package com.nosolojava.fsm.model.state;

import com.nosolojava.fsm.model.transition.Transition;

public interface HistoryState{

	String getId();
	HistoryTypes getType();
	
	Transition getTransition();
}
