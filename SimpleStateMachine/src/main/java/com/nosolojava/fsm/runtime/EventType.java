package com.nosolojava.fsm.runtime;

public enum EventType {

	PLATFORM("platform"),INTERNAL("internal"),EXTERNAL("external");
	
	
	

	private EventType(String type) {
		this.type = type;
	}

	private String type;

	@Override
	public String toString() {
		return type;
	}
	
	
	
	
}
