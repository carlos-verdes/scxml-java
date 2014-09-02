package com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic;

import java.io.Serializable;
import java.net.URI;

import com.nosolojava.fsm.impl.runtime.executable.externalcomm.io.ScxmlIOProcessor;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public class BasicMessage implements Message {

	private static final long serialVersionUID = -7099806219356446943L;

	private final String id;
	private final String name;
	private final URI source;
	private final URI target;
	private final Object body;

	public BasicMessage(String id, String name, URI source, URI target, Object body) {
		super();
		this.id = id;
		this.name = name;
		this.source = source;
		this.target = target;
		this.body = body;
	}

	public static Message createSimpleSCXMLMessage(String eventName, Context context) {
		return createSimpleSCXMLMessage(eventName, null, null, context);
	}

	public static Message createSimpleSCXMLMessage(String eventName, String targetString, Context context) {
		return createSimpleSCXMLMessage(eventName, targetString, null, context);
	}

	public static Message createSimpleSCXMLMessage(String eventName, String targetString, Serializable body,
			Context context) {

		String id = "";
		String name = eventName;

		ScxmlIOProcessor scxmlIOProcessor = (ScxmlIOProcessor) context.searchIOProcessor(ScxmlIOProcessor.NAME);
		URI sourceURI = scxmlIOProcessor.getLocation(context);
		URI targetURI;
		if (targetString != null) {
			targetURI = scxmlIOProcessor.getLocation(targetString);
		} else {
			targetURI = sourceURI;
		}

		BasicMessage message = new BasicMessage(id, name, sourceURI, targetURI, body);

		return message;

	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public URI getTarget() {
		return this.target;
	}

	@Override
	public URI getSource() {
		return this.source;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getBody() {
		return (T) this.body;
	}

}
