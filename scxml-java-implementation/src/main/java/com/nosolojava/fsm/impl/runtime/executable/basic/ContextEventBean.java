package com.nosolojava.fsm.impl.runtime.executable.basic;

import java.io.Serializable;

import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.Event;

public class ContextEventBean implements Serializable {
	private static final long serialVersionUID = 1385100231950086818L;
	private final Context context;
	private final Event event;

	public ContextEventBean(Context context, Event event) {
		super();
		this.context = context;
		this.event = event;
	}

	public Context getContext() {
		return context;
	}

	public Event getEvent() {
		return event;
	}

}
