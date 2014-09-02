package com.nosolojava.fsm.runtime;

import java.net.URI;

public interface Event {

	String getName();
	
	EventType getType();
	
	String getSendId();
	
	URI getOrigin();
	
	String getOriginType();
	
	String getInvokeId();

	<T> T getData();

}
