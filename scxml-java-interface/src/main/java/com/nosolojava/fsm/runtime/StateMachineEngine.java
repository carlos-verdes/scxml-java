package com.nosolojava.fsm.runtime;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.parser.StateMachineParser;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.InvokeHandler;

public interface StateMachineEngine {

	void start();

	void forceShutdown();

	boolean shutdownAndWait(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * Used to start a SCXML session (normal scenario).
	 * 
	 * @param fsmModelURI
	 *            FSM model source
	 * @return the current FSM config (after initialization)
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	Context startFSMSession(URI fsmModelUri) throws ConfigurationException, IOException,SCXMLParserException;

	/**
	 * Used to start a SCXML session with a parent session.
	 * 
	 * @param parentSessionId
	 *            parent scxml session id (null could be passed if this doesn't apply
	 * @param fsmModelURI
	 *            FSM model source
	 * @return the current FSM config (after initialization)
	 * @throws IOException
	 */
	Context startFSMSession(String parentSessionId, URI fsmModelUri) throws ConfigurationException, IOException,SCXMLParserException;

	/**
	 * Used to start a SCXML session when the session id is known. For example when an {@link Invoke} of type "scxml" is
	 * called.
	 * 
	 * @param sessionId
	 *            scxml session id
	 * @param parentSessionId
	 *            parent scxml session id (null could be passed if this doesn't apply
	 * @param fsmModelURI
	 *            FSM model source
	 * @param initValues
	 *            invoke params used to initialize the called session
	 * @return the current FSM config (after initialization)
	 * @throws ConfigurationException
	 * @throws IOException
	 */

	Context startFSMSession(String sessionId, String parentSessionId, URI fsmModelUri,
			Map<String, Serializable> initValues) throws ConfigurationException, IOException,SCXMLParserException;

	/**
	 * Checks if there is an FSM session active with the indicated session id.
	 * 
	 * @param sessionId
	 * @return
	 */
	boolean isSessionActive(String sessionId);

	/**
	 * 
	 * @return the active sessions
	 */
	Collection<Context> getActiveSessions();

	/**
	 * Get a session by id.
	 * 
	 * @param sessionId
	 * @return
	 */
	Context getSession(String sessionId);

	/**
	 * When a FSM session finish this method is called to cleanup internal variables and to avoid more events to be
	 * processed.
	 * 
	 * @param sessionId
	 */
	void endSession(String sessionId);

	/**
	 * Look up the FSM session and push an event to its external event queue.
	 * 
	 * @param sessionId
	 * @param event
	 */
	void pushEvent(String sessionId, Event event);

	Set<IOProcessor> getIOProcessors();

	IOProcessor getIOProcessor(String name);

	void registerIOProcessor(IOProcessor ioprocessor);

	void unRegisterIOProcessor(String name);

	Set<InvokeHandler> getInvokeHandlers();

	InvokeHandler getInvokeHandler(String type);

	void registerInvokeHandler(InvokeHandler invokeHandler);

	void unRegisterInvokeHandler(String type);

	void setParser(StateMachineParser parser);

	StateMachineParser getParser();

	void setStateMachineFramework(StateMachineFramework framework);

	StateMachineFramework getStateMachineFramework();

	/**
	 * This factory will be used when the engine starts a new fsm session
	 * 
	 * @param contextFactory
	 */
	void setContextFactory(ContextFactory contextFactory);

	/**
	 * Used to change default log (useful in android).
	 * 
	 * @param logCallback
	 */
	void setLogCallback(FSMLogCallback logCallback);

	/**
	 * Used to get the same logger as the fsm.
	 * 
	 * @return
	 */
	FSMLogCallback getLogCallback();
}
