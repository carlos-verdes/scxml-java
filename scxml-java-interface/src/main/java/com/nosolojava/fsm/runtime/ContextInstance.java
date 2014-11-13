package com.nosolojava.fsm.runtime;

import java.util.Map;
import java.util.SortedSet;

import com.nosolojava.fsm.model.state.State;

/**
 * <p>Context instance info sent to listeners to avoid changes on FSM session from outside. 
 * <br/>The model should be observed only when a macro step has finished to avoid incomplete configurations.
 * <ul>Information provided
 * <li>session id
 * <li>parent session id
 * <li>active states
 * <li>complete key-value model
 * @author cverdes
 *
 */
public interface ContextInstance {

	String getSessionId();
	String getParentSessionId();
	SortedSet<State> getActiveStates();
	<T> T getDataByName(String name);
	<T> T getDataByExpression(String expression);
}
