package com.nosolojava.fsm.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.datamodel.URLDataHandler;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeHandler;

/**
 * This interface has the current configuration of the FSM: active states, current event, etc.
 * 
 * 
 * @author Carlos Verdes
 * 
 */
public interface Context {

	/**
	 * @return scxml session id. This is used to identify this session by event handlers.
	 */
	String getSessionId();

	/**
	 * 
	 * @return the session name (the name attribute of the element scxml, for example on the scxml
	 *         {@code   <scxml name="MyStateMachine">...</scxml>} this method will return "MyStateMachine".
	 */
	String getSessionName();

	/**
	 * @return the parent session id if exists, null if there is none.
	 */
	String getParentSessionId();

	/**
	 * This is used to send/raise events to generate the origin attribute on the event to be sent.
	 * 
	 * @param name
	 *            the type of processor needed (for example http://www.w3.org/TR/scxml/#SCXMLEventProcessor name).
	 * @return the {@link IOProcessor} from which the event handler could get the location for this session.
	 */
	IOProcessor searchIOProcessor(String name);

	IOProcessor getScxmlIOProcessor();

	/**
	 * 
	 * @return all the processors that are supported
	 */
	Set<IOProcessor> getIOProcessors();

	/**
	 * 
	 * @return the invoke handlers registered for this context (FSM config).
	 */
	Set<InvokeHandler> getInvokeHandlers();

	/**
	 * 
	 * @param type
	 *            the type of the invoke handler
	 * @return
	 */
	InvokeHandler getInvokeHandler(String type);

	/**
	 * @return the FSM model (static definition of states, transitions, etc.).
	 */
	StateMachineModel getModel();

	/**
	 * @return the current event.
	 */
	Event getCurrentEvent();

	void setCurrentEvent(Event event);

	/**
	 * Adds an event to the external event queue.
	 * 
	 * @param event
	 */
	void offerExternalEvent(String eventName);

	/**
	 * Adds an event to the external event queue.
	 * 
	 * @param event
	 */
	void offerExternalEvent(String eventName, Object data);

	/**
	 * Adds an event to the external event queue.
	 * 
	 * @param event
	 */
	void offerExternalEvent(Event event);

	boolean hasExternalEvents();

	/**
	 * Retrieves the next external event. Blocks if empty.
	 * 
	 * @return
	 */
	Event pollExternalEvent();

	/**
	 * 
	 * @return true if there is an internal event or more
	 */
	boolean hasInternalEvents();

	/**
	 * @return the next internal event. This method doesn't block if empty but hasInternalEvents() method should be used
	 *         before calling this.
	 */
	Event pollInternalEvent();

	/**
	 * Register a new internal event that will be processed by the FSM on the current macrostep.
	 * 
	 * @param event
	 */
	void offerInternalEvent(Event event);

	/**
	 * When a invoke is executed this method is called to register in the context so any event sent to this invoke
	 * session could be managed
	 * 
	 * @param invokeSessionId
	 */
	void registerInvokeSessionId(String invokeSessionId);

	/**
	 * When an invoke is canceled then this method is called to avoid the process of more events (to this invoke
	 * session).
	 * 
	 * @param invokeSessionId
	 */
	void unRegisterInvokeSessionId(String invokeSessionId);

	/**
	 * This method is called to check if the invoked session is active.
	 * 
	 * @param invokeSessionId
	 * @return true if the invoke session is active
	 */
	boolean isInvokeSessionActive(String invokeSessionId);

	/**
	 * Used by the FSM to clear the states to invoke (after invoke them)
	 */
	void clearStatesToInvoke();

	void addStateToInvoke(State state);

	void removeStateToInvoke(State state);

	SortedSet<State> getStatesToInvoke();

	<T> T getDataByName(String name);

	<T> T getDataByExpression(String expression);

	void createVarIfDontExist(String name, Object value);

	void updateDataIfExists(String id, Object value);

	boolean existsVarName(String id);

	void updateData(String id, Object value);

	boolean evaluateConditionGuardExpresion(String expresion);

	boolean isActiveStateByName(String stateName);

	boolean isActiveState(State state);

	SortedSet<State> getActiveStates();

	void addActiveState(State state);

	void removeActiveState(State state);

	<T> T getDataFromURL(URL url);

	void addURLDataHandler(URLDataHandler var);

	void addURLDataHandlers(List<URLDataHandler> vars);

	void clearAndSetURLDataHandlers(List<URLDataHandler> vars);

	State getState(String targetState);

	void executeScript(String code);

	void executeScript(URL codeUri) throws IOException;
}
