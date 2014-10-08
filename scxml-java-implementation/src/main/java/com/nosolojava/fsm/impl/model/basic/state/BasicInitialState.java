package com.nosolojava.fsm.impl.model.basic.state;

import java.util.UUID;

import com.nosolojava.fsm.impl.model.basic.transition.BasicTransition;
import com.nosolojava.fsm.model.state.InitialState;
import com.nosolojava.fsm.model.transition.Transition;

public class BasicInitialState implements InitialState {
	private static final long serialVersionUID = -2412563275949701163L;
	private final Transition initialTransition;
	private final String id;

	public BasicInitialState(String id, Transition initialTransition) {
		super();

		this.initialTransition = initialTransition;

		if (id == null || "".equals(id)) {
			id = UUID.randomUUID().toString();
		}
		this.id = id;

	}

	public BasicInitialState(String id, String state, String initialState) {
		super();
		this.initialTransition = new BasicTransition(state, initialState);
		this.id = id;

	}

	@Override
	public Transition getInitialTransition() {
		return initialTransition;
	}

	@Override
	public String getId() {
		return this.id;
	}

}
