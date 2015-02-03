package com.nosolojava.fsm.impl.runtime.executable.externalcomm.io;

import java.net.URI;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.impl.runtime.basic.PlatformEvents;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.EventType;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

/**
 * Implements SXML IO processor: http://www.w3.org/TR/scxml/#SCXMLEventProcessor
 * 
 * @author Carlos Verdes
 * 
 */
public class ScxmlIOProcessor implements IOProcessor {
	Logger logger = Logger.getLogger(this.getClass().getName());
	public static final String NAME = "scxml";
	public static final String _INTERNAL = "_internal";
	public static final String _PARENT = "_parent";
	public static final String SCXML_SESSION_REGEXP = "_scxml_(.*)";
	private final MessageFormat scxmlMF = new MessageFormat("#_scxml_{0}");
	public static final Pattern SCXML_SESSION_PATTERN = Pattern.compile(SCXML_SESSION_REGEXP);
	public static final String SCXML_INVOKEID_REGEXP = "_(.*)";
	public static final Pattern SCXML_INVOKEID_PATTERN = Pattern.compile(SCXML_INVOKEID_REGEXP);
	private static final String $1 = "$1";

	private StateMachineEngine engine;

	public ScxmlIOProcessor(StateMachineEngine engine) {
		super();
		this.engine = engine;
	}

	public static ScxmlIOProcessor getInstance(Context context) {
		ScxmlIOProcessor scxmlIOProcessor = (ScxmlIOProcessor) context.searchIOProcessor(ScxmlIOProcessor.NAME);
		return scxmlIOProcessor;

	}

	public static URI getSourceFromContext(Context context) {
		ScxmlIOProcessor instance = ScxmlIOProcessor.getInstance(context);
		return instance.getLocation(context);

	}

	public static URI getParentSessionSource(Context context) {
		ScxmlIOProcessor instance = ScxmlIOProcessor.getInstance(context);

		String parentSessionId = context.getParentSessionId();
		return instance.getLocation(parentSessionId);

	}

	public URI getLocation(Context context) {
		URI uri = null;

		String id = context.getSessionId();
		uri = getLocation(id);

		return uri;
	}

	@Override
	public URI getLocation(String sessionId) {
		String idURIString = scxmlMF.format(new Object[] { sessionId });
		URI result = null;

		result = URI.create(idURIString);

		return result;

	}

	@Override
	public void sendMessageFromFSM(Message message) {
		// TODO manage errors when resolving an event in ScxmlIOProcessor

		Context originContext = getSessionFromURI(message.getSource());

		if (originContext != null) {

			URI target = message.getTarget();

			//if the target is null, then forward to the origin session
			if (target == null) {
				createAndpushEventToFSM(message, originContext);

			} else {
				//get the target fragment
				String targetFragment = target.getFragment();

				//if the target is "_internal"
				if (_INTERNAL.equals(targetFragment)) {
					// offer to internal event queue of the origin context
					Event event = createEventFromMessage(message, EventType.INTERNAL, true);
					originContext.offerInternalEvent(event);

					// if the target is "_parent"
				} else if (_PARENT.equals(targetFragment)) {
					//then send to the parent session
					String parentId = originContext.getParentSessionId();

					Context parentContext = this.engine.getSession(parentId);

					if (parentContext != null) {
						// create an event with origin invoke id
						Event event = createEventFromChildren(message, EventType.EXTERNAL, originContext.getSessionId());

						// push to the fsm
						pushEventToFSM(event, parentContext);

					} else {
						logger.log(Level.SEVERE,
								"Error managing scxml send target: {0}. The parent id {1} has not beed found.",
								new Object[] { target, parentId });
						//send error.communication error event 
						sendCommErrorEvent(originContext);

					}

					// else, get scxml session id
				} else {
					String targetSession = this.getSessionIdFromURIFragment(targetFragment);
					//if the target is an scxml session id
					if (isScxmlURI(targetFragment)) {
						//get the session
						Context targetContext = this.engine.getSession(targetSession);

						// push the event to that session
						createAndpushEventToFSM(message, targetContext);

						//if the event is an invoke id
					} else if (isInvokeURI(targetFragment)) {
						String invokeId = targetSession;

						//check if the invoke id exists
						if (originContext.isInvokeSessionActive(invokeId)) {
							//then, push the event to the origin session
							createAndpushEventToFSMFromInvoke(message, originContext);

						} else {
							logger.log(Level.WARNING, "The invoke session: {0} is not active in context: {1}.",
									new Object[] { invokeId, originContext.getSessionId() });

							//send error.communication error event 
							sendCommErrorEvent(originContext);
						}

					} else if (engine.isSessionActive(targetFragment)) {
						Context targetContext = engine.getSession(targetFragment);
						createAndpushEventToFSM(message, targetContext);
					} else {
						logger.log(Level.SEVERE, "Error managing scxml send target: {0}.", new Object[] { target });
						//send error.communication error event 
						sendCommErrorEvent(originContext);

					}
				}
			}

		} else {
			//this should never occur
			logger.log(Level.SEVERE, "Error managing scxml send, cant find the origin session: {0}.",
					new Object[] { message.getSource() });
		}

	}

	protected void sendCommErrorEvent(Context originContext) {
		Event commErrorEvent = createCommErrorEvent(originContext);
		originContext.offerInternalEvent(commErrorEvent);
	}

	protected Event createCommErrorEvent(Context originContext) {
		Event commErrorEvent = BasicEvent.createPlatforEvent(PlatformEvents.COMMUNICATION_ERROR.getEventName(),
				this.getLocation(originContext), null);
		return commErrorEvent;
	}

	protected void createAndpushEventToFSM(Message message, Context context) {

		// create an external event
		EventType eventType = EventType.EXTERNAL;
		Event event = createEventFromMessage(message, eventType, true);

		// push to the fsm
		pushEventToFSM(event, context);

	}

	protected void createAndpushEventToFSMFromInvoke(Message message, Context context) {
		// create an external event
		EventType eventType = EventType.EXTERNAL;
		Event event = createEventFromMessage(message, eventType, false);

		// push to the fsm
		pushEventToFSM(event, context);

	}

	protected Event createEventFromChildren(Message message, EventType eventType, String originSessionId) {
		String eventName = message.getName();

		URI origin = message.getSource();
		String originType = NAME;

		// the invoke id will be the origin session id
		String sendId = "";
		String invokeId = originSessionId;

		Object data = message.getBody();

		Event event = new BasicEvent(eventName, eventType, sendId, origin, originType, invokeId, data);
		return event;
	}

	protected Event createEventFromMessage(Message message, EventType eventType, boolean isSend) {
		String eventName = message.getName();

		URI origin = message.getSource();
		String originType = ScxmlIOProcessor.NAME;

		// the message id will be the send id
		String sendId;
		String invokeId;
		if (isSend) {
			sendId = message.getId();
			invokeId = "";
		} else {
			sendId = "";
			invokeId = message.getId();
		}

		Object data = message.getBody();

		Event event = new BasicEvent(eventName, eventType, sendId, origin, originType, invokeId, data);
		return event;
	}

	@Override
	public void sendEventToFSM(String sessionId, Event event) {
		if(this.engine.isSessionActive(sessionId)){
			this.engine.pushEvent(sessionId, event);
		}
		
	}
	
	protected void pushEventToFSM(final Event event, final Context context) {

		String sessionId = context.getSessionId();
		sendEventToFSM(sessionId, event);
	}

	private Context getSessionFromURI(URI target) {
		Context context = null;

		String targetFragment = target.getFragment();
		String sessionId = getSessionIdFromURIFragment(targetFragment);

		context = this.engine.getSession(sessionId);

		return context;
	}

	private String getSessionIdFromURIFragment(String uriFragment) {
		String targetSessionId = null;
		Matcher matcher = SCXML_SESSION_PATTERN.matcher(uriFragment);
		if (matcher.matches()) {
			// target is a scxml session
			targetSessionId = matcher.replaceAll($1);
		} else {

			matcher = SCXML_INVOKEID_PATTERN.matcher(uriFragment);
			if (matcher.matches()) {
				// target is an invoke session id
				targetSessionId = matcher.replaceAll($1);
			}
		}
		return targetSessionId;
	}

	protected boolean isScxmlURI(String uriFragment) {
		Matcher matcher = SCXML_SESSION_PATTERN.matcher(uriFragment);
		boolean isScxmlSessionId = matcher.matches();
		return isScxmlSessionId;
	}

	protected boolean isInvokeURI(String uriFragment) {
		Matcher matcher = SCXML_INVOKEID_PATTERN.matcher(uriFragment);
		boolean isScxmlSessionId = matcher.matches();
		return isScxmlSessionId;
	}

	@Override
	public String getName() {
		return NAME;
	}

	public StateMachineEngine getEngine() {
		return engine;
	}

	@Override
	public void setEngine(StateMachineEngine engine) {
		this.engine = engine;
	}

	public static void main(String[] args) {

		String a= "android://action/#sessionId";
		
		MessageFormat uriMF = new MessageFormat("android://{0}/{1}?{2}#{3}");
		a= uriMF.format(new Object[]{ScxmlIOProcessor.class.getCanonicalName(),"","","sessionid"});

		URI uri = URI.create(a);
		
		System.out.println("auth: "+uri.getAuthority());
		System.out.println("path: "+uri.getPath());
		System.out.println("specific: "+uri.getFragment());
	}
}
