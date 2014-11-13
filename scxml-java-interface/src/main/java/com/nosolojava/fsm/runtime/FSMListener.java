package com.nosolojava.fsm.runtime;


/**
 * <p>Listener to FSM changes on sessions:
 * <ul>
 * <li>on session start
 * <li>on session finish
 * <li>on new state
 * 
 * <p>Notice that 
 * @author cverdes
 *
 */
public interface FSMListener {

	void onSessionStarted(ContextInstance context);
	
	void onSessionEnd(ContextInstance context);
	
	void onNewState(ContextInstance context);

}
