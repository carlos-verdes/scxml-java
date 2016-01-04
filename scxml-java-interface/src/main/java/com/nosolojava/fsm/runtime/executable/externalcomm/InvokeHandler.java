package com.nosolojava.fsm.runtime.executable.externalcomm;

import java.io.Serializable;

import com.nosolojava.fsm.runtime.Context;

public interface InvokeHandler extends Serializable {
	String getType();

	/**
	 * This method is called when a state with an invoke is reached (onentry).
	 * 
	 * @param input invoke info like session id, source or content input data and additional arguments.
	 * @param context fsm current configuration
	 */
	void invokeService(InvokeInfo input, Context context);

	
	/**
	 * If the invoke has the attribute auto forward, then the FSM will send the external events to the invoke.
	 * 
	 * @param message
	 */
	void sendMessageToService(Message message);
	

	/**
	 * When the event done.invoke.id (where id is the invoke id) or when the state which this invoke is placed exits,
	 * then the session for this invoke should be finished an cleared (no more events should be managed).
	 * 
	 * @param invokeId
	 *            the invoke session id that has to be finished
	 */
	void endSession(String invokeId);
	
}
