package com.nosolojava.fsm.impl.runtime.basic;


public enum PlatformEvents {

	COMMUNICATION_ERROR("error.communication"),DONE_INVOKE_PREFIX("done.invoke."),DONE_STATEPREFIX("done.state."), EXECUTION_ERROR("error.execution");

	private String eventName;

	private PlatformEvents(String eventName) {
		this.eventName = eventName;
	}

	public String getEventName() {
		return this.eventName;
	}
}
