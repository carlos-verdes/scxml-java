package com.nosolojava.fsm.model.state;

import java.io.Serializable;

import com.nosolojava.fsm.model.transition.Transition;


public interface InitialState extends Serializable{

	String getId();
	Transition getInitialTransition();

}
