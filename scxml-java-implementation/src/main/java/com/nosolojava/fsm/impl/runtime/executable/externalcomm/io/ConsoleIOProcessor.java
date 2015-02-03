package com.nosolojava.fsm.impl.runtime.executable.externalcomm.io;

import java.net.URI;

import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public class ConsoleIOProcessor implements IOProcessor {
	StateMachineEngine engine = null;

	public static String NAME = "console";
	URI internalURI = URI.create("console");

	public ConsoleIOProcessor(StateMachineEngine engine) {
		super();
		this.engine = engine;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public URI getLocation(String sessionId) {
		return this.internalURI;
	}

	@Override
	public void sendMessageFromFSM(Message message) {
		if (message != null && message.getBody() != null
				&& String.class.isAssignableFrom(message.getBody().getClass())) {
			String messageText = (String) message.getBody();
			if (messageText != null) {
				System.out.println("> " + messageText);

			}
		}

	}

	@Override
	public void setEngine(StateMachineEngine engine) {
		this.engine = engine;
	}

	@Override
	public void sendEventToFSM(String sessionId, Event event) {
		if(this.engine.isSessionActive(sessionId)){
			this.engine.pushEvent(sessionId, event);
		}
		
	}

}
