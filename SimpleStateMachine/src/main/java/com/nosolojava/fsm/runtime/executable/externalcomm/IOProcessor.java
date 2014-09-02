package com.nosolojava.fsm.runtime.executable.externalcomm;

import java.io.Serializable;
import java.net.URI;

import com.nosolojava.fsm.model.externalcomm.Send;
import com.nosolojava.fsm.runtime.StateMachineEngine;

/**
 * This interface is used by the {@link Send} to send events from FSM and to receive events from external systems.
 * <p>
 * The external event handling will be dependant on the implementation (so it's not defined on this interface) but
 * should use the next method: {@link StateMachineEngine#pushEvent(String, com.nosolojava.fsm.runtime.Event)}.
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
	 * Sends a messageto the selected target.
	 * 
	 * @param message
	 *            message to send
	 */
	void sendMessage(Message message);

	void setEngine(StateMachineEngine engine);
}
