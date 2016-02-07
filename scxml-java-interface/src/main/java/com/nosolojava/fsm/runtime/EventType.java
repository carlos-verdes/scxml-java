package com.nosolojava.fsm.runtime;

public enum EventType {

	PLATFORM("PLATFORM"),INTERNAL("INTERNAL"),EXTERNAL("EXTERNAL");
	
	private EventType(String type) {
		this.type = type;
	}

	private String type;

	@Override
	public String toString() {
		return type;
	}
	
	
	
	
}
