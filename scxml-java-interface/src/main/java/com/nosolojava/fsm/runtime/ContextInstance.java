package com.nosolojava.fsm.runtime;

import java.util.List;
import java.util.Set;

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
	List<String> getActiveStates();
	Set<String> dataModelKeySet();
	<T> T getDataByName(String name);
	<T> T getDataByExpression(String expression);
}
