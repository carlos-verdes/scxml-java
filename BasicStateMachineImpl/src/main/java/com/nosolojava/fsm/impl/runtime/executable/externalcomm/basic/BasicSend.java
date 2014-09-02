package com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic;

import java.net.URI;
import java.util.UUID;

import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineFramework;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.datamodel.Body;
import com.nosolojava.fsm.model.externalcomm.Send;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public class BasicSend implements Send {
	private static final long serialVersionUID = -3511221215537288327L;
	//	private final Logger logger = Logger.getLogger(this.getClass().getName());
	public static final String _INTERNAL = "_internal";

	private final String id;
	private final String idLocation;

	private final String eventName;
	private final String eventExpression;

	private final String type;
	private final String typeExpression;

	private final URI target;
	private final String targetExpression;

	private final Long delay;
	private final String delayExpression;

	/* it represents namelist, params and content*/
	private final Body body;

	private final String toText;

	public BasicSend(String id, String idLocation, String eventName, String eventExpression, String type,
			String typeExpression, URI target, String targetExpression, Long delay, String delayExpression, Body body)
			throws ConfigurationException {
		super();

		if (id != null && idLocation != null) {
			throw new ConfigurationException("Cant be Id and Idlocation in the same send element.");
		}
		if (eventName != null && eventExpression != null) {
			throw new ConfigurationException("Cant be event and eventExpression in the same send element.");
		}
		if (type != null && typeExpression != null) {
			throw new ConfigurationException("Cant be type and typeExpression in the same send element.");
		}
		if (target != null && targetExpression != null) {
			throw new ConfigurationException("Cant be target and targetExpression in the same send element.");
		}
		if (delay != null && delayExpression != null) {
			throw new ConfigurationException("Cant be delay and delayExpression in the same send element.");
		}

		this.id = id;
		this.idLocation = idLocation;
		this.eventName = eventName;
		this.eventExpression = eventExpression;
		this.type = type;
		this.typeExpression = typeExpression;
		this.target = target;
		this.targetExpression = targetExpression;
		this.delay = delay;
		this.delayExpression = delayExpression;

		this.body = body;

		this.toText = loadToText();
	}

	protected Message createMessage(Context context) {
		Message message = null;

		String name = this.getEventName(context);
		URI source = getSource(context);
		URI target = this.getTarget(context);

		String sendId;
		if (this.idLocation != null) {
			//the id should be generated and stored in idlocation
			String result = UUID.randomUUID().toString();
			context.updateData(this.idLocation, result);
		}

		sendId = this.getId(context);

		Object body = this.getBody(context);
		message = new BasicMessage(sendId, name, source, target,  body);

		return message;
	}

	private URI getSource(Context context) {
		IOProcessor ioProcessor = context.searchIOProcessor(this.getType(context));
		URI source = ioProcessor.getLocation(context.getSessionId());

		return source;
	}

	private String loadToText() {
		String result;
		if (BasicStateMachineFramework.DEBUG.get()) {
			StringBuilder sb = new StringBuilder("Send [type=");
			if (this.type != null) {
				sb.append(this.type);
			} else {
				sb.append(this.typeExpression);
			}

			sb.append(", event=");
			if (this.eventName != null) {
				sb.append(this.eventName);
			} else {
				sb.append(this.eventExpression);
			}

			sb.append(", target=");
			if (this.target != null) {
				sb.append(this.target);
			} else {
				sb.append(this.targetExpression);
			}
			sb.append("]");
			result = sb.toString();
		} else {
			result = "Send ";
		}
		return result;
	}

	@Override
	public String getId(Context context) {
		String result;

		if (this.id != null) {
			result = this.id;
		} else {
			result = context.getDataByName(this.idLocation);
		}

		return result;
	}

	@Override
	public String getEventName(Context context) {
		String result = null;

		if (this.eventName != null) {
			result = this.eventName;
		} else if (this.eventExpression != null) {
			Object event = context.getDataByExpression(this.eventExpression);
			if (event != null) {
				if (event.getClass().isAssignableFrom(Event.class)) {
					result = ((Event) event).getName();
				} else if (event.getClass().isAssignableFrom(String.class)) {
					result = (String) event;
				} else {
					throw new RuntimeException("Event class not supported " + event.getClass() + ": " + event);
				}
			}
		}

		return result;
	}

	@Override
	public String getType(Context context) {
		String result;

		if (this.type == null && this.typeExpression == null) {
			result = context.getScxmlIOProcessor().getName();
		} else {
			if (this.type != null) {
				result = this.type;
			} else {
				result = context.getDataByExpression(this.typeExpression);
			}

		}

		return result;
	}

	@Override
	public URI getTarget(Context context) {
		URI result = null;

		if (this.target != null) {
			result = this.target;
		} else if (this.targetExpression != null) {
			Object auxValue = context.getDataByExpression(targetExpression);

			if (auxValue != null && String.class.isAssignableFrom(auxValue.getClass())) {
				result = URI.create("" + auxValue);
			} else if (auxValue != null && URI.class.isAssignableFrom(auxValue.getClass())) {
				result = (URI) auxValue;
			}
		}

		return result;

	}

	@Override
	public long getDelay(Context context) {
		long result;

		if (this.delay != null) {
			result = this.delay;
		} else {
			result = context.getDataByExpression(delayExpression);
		}
		return result;
	}

	@Override
	public Object getBody(Context context) {
		Object result = this.body.evaluateBody(context);

		return result;
	}

	@Override
	public void run(Context context) {

		Message message;
		message = this.createMessage(context);

		IOProcessor ioProcessor = context.searchIOProcessor(this.getType(context));
		ioProcessor.sendMessage(message);

	}

	@Override
	public String toString() {
		return this.toText;
	}

}
