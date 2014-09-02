package com.nosolojava.fsm.impl.model.basic.state;

import java.io.Serializable;

import com.nosolojava.fsm.model.datamodel.Body;
import com.nosolojava.fsm.model.state.DoneData;
import com.nosolojava.fsm.runtime.Context;

public class BasicDoneData implements DoneData {

	private static final long serialVersionUID = -1244917349410062609L;
	private final Body body;

	public BasicDoneData(Body body) {
		this.body = body;
	}

	@Override
	public Serializable evaluateDoneData(Context context) {

		Serializable result = null;
		if (this.body != null) {
			result = this.body.evaluateBody(context);
		}

		return result;
	}
}
