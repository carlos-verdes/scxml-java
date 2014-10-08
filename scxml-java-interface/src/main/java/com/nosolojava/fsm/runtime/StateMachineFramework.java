package com.nosolojava.fsm.runtime;

import com.nosolojava.fsm.model.config.exception.ConfigurationException;

public interface StateMachineFramework {

	/**
	 * Get a new FSM context and initialize it.
	 * 
	 * @param context
	 *            current FSM configuration
	 * @throws ConfigurationException
	 */
	void initFSM(Context context) throws ConfigurationException;

	/**
	 * Based on the current FSM configuration this method execute a macrostep (with all the microsteps associated). This
	 * will be called from the {@link StateMachineEngine}.
	 * 
	 * @param event
	 *            event to do the macrostep
	 * @param context
	 *            current FSM config
	 */
	void handleExternalEvent(Event event, Context context);

	void registerListener(FSMListener listener);

	void unRegisterListener(FSMListener listener);

	void setEngine(StateMachineEngine engine);
}
