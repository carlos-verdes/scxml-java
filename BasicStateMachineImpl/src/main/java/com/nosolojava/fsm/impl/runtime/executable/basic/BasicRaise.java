package com.nosolojava.fsm.impl.runtime.executable.basic;

import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.executable.Raise;

public class BasicRaise implements Raise {
	private static final long serialVersionUID = 7308450602567632888L;

	private final String eventName;

	public BasicRaise(String event) {
		super();
		this.eventName = event;
	}

	@Override
	public void run(Context context) {
		Event event = new BasicEvent(eventName);
		context.offerInternalEvent(event);
	}

}
