package com.nosolojava.fsm.impl.runtime.basic;

import java.net.URI;
import java.net.URISyntaxException;

import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.BasicSend;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.io.ScxmlIOProcessor;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.EventType;

public class BasicEvent implements Event {

	private final String name;
	private final EventType eventType;
	private final String sendId;
	private final URI origin;
	private final String originType;
	private final String invokeId;
	private final Object data;

	private static URI INTERNAL_URI;
	static {
		try {
			INTERNAL_URI = new URI("#" + BasicSend._INTERNAL);
		} catch (URISyntaxException e) {
			//never happen
		}

	}

	public BasicEvent(String name) {
		this(name, null);
	}

	public BasicEvent(String name, Object data) {
		this(name, data, "");
	}

	public BasicEvent(String name, Object data, String invokeId) {
		this(name, EventType.INTERNAL, "", INTERNAL_URI, "", invokeId, data);
	}

	public BasicEvent(String name, EventType eventType, String sendId, URI origin, String originType, String invokeId,
			Object data) {
		super();
		this.name = name;
		this.eventType = eventType;
		this.sendId = sendId;
		this.origin = origin;
		this.originType = originType;
		this.invokeId = invokeId;
		this.data = data;
	}

	public static Event createPlatforEvent(String eventName,URI origin, Object data) {
		Event result= new BasicEvent(eventName, EventType.PLATFORM, "",origin, ScxmlIOProcessor.NAME,"", data);
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicEvent other = (BasicEvent) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BasicEvent [name=" + name + ", eventType=" + eventType + ", sendId=" + sendId + ", origin=" + origin
				+ ", originType=" + originType + ", invokeId=" + invokeId + ", data=" + data + "]";
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getData() {
		return (T) data;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getInvokeId() {
		return invokeId;
	}

	@Override
	public EventType getType() {
		return this.eventType;
	}

	@Override
	public String getSendId() {
		return this.sendId;
	}

	@Override
	public URI getOrigin() {
		return this.origin;
	}

	@Override
	public String getOriginType() {
		return this.originType;
	}

}
