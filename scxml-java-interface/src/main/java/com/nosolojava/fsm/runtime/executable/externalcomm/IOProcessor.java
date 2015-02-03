package com.nosolojava.fsm.runtime.executable.externalcomm;

import java.net.URI;

import com.nosolojava.fsm.model.externalcomm.Send;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.StateMachineEngine;

/**
 * This interface is used by the {@link Send} to send events from FSM and to receive events from external systems (FSM
 * receives events and sends messages). <br/>
 * It will be associated with a state machine engine so the FSM context could find this processor in order to send
 * messages to external resources (or another sessions) and the IOProcessor could use the engine to send events to the FSM.
 * <p>
 * 
 * @author Carlos Verdes
 * 
 */
public interface IOProcessor {

	/**
	 * The name is used to identify which IO processor should be used to manage the send action.
	 * 
	 * @return the name of this IO processor
	 */
	String getName();

	/**
	 * @param sessionId
	 *            session id
	 * @return the uri that the receiver could use to send an event back to the FSM.
	 */
	URI getLocation(String sessionId);

	/**
	 * Sends a message from SCXML session to the selected target.
	 * 
	 * @param message
	 *            message to send
	 */
	void sendMessageFromFSM(Message message);

	/**
	 * Sends a message from an external resource to a SCXML session
	 * 
	 * @param engine
	 */

	void sendEventToFSM(String sessionId, Event event);

	/**
	 * Used by the engine to register this processor.
	 * 
	 * @param engine
	 */
	void setEngine(StateMachineEngine engine);
}
