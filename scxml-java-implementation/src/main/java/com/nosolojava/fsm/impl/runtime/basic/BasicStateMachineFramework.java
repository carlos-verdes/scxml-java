package com.nosolojava.fsm.impl.runtime.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.BasicMessage;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.config.exception.ParallelSiblingTransactionException;
import com.nosolojava.fsm.model.externalcomm.Invoke;
import com.nosolojava.fsm.model.state.DoneData;
import com.nosolojava.fsm.model.state.HistoryTypes;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.model.transition.Transition;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.FSMListener;
import com.nosolojava.fsm.runtime.FSMLogCallback;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.StateMachineFramework;
import com.nosolojava.fsm.runtime.executable.Executable;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public class BasicStateMachineFramework implements StateMachineFramework {

	private static final String HISTORY_STATES = "_historyStates";

	public static final AtomicBoolean DEBUG = new AtomicBoolean(false);

	//	private final Map<String, InvokeHandler> invokeHandlers = new ConcurrentHashMap<String, InvokeHandler>();

	private final CopyOnWriteArrayList<FSMListener> listeners = new CopyOnWriteArrayList<FSMListener>();

	private FSMLogCallback logCallback;

	private StateMachineEngine engine = null;

	public BasicStateMachineFramework() {
		this(null);
	}

	public BasicStateMachineFramework(FSMLogCallback logCallback) {
		this(logCallback, null);

	}

	public BasicStateMachineFramework(FSMLogCallback logCallback, List<FSMListener> listeners) {

		if (logCallback == null) {
			logCallback = createBasicLogCallback();
		}
		this.logCallback = logCallback;

		if (listeners != null && !listeners.isEmpty()) {
			this.listeners.addAll(listeners);
		}
	}

	@Override
	public void initFSM(Context context) throws ConfigurationException {
		if (DEBUG.get()) {
			logFine("####################################################################################");
			logFine("init FSM sessionid {0}", new Object[] { context.getSessionId() });
		}

		// create a history variable
		context.createVarIfDontExist(HISTORY_STATES, new HashMap<String, List<State>>());

		//get the model
		StateMachineModel model = context.getModel();

		// validate states
		validateStates(context, model.getRootState());

		// get the initial transition
		Transition initialTransition = model.getRootState().getInitialState().getInitialTransition();

		// execute transitions execs
		executeContent(context, initialTransition.getExecutables());

		// entry initial state
		enterState(context, initialTransition);

		// first step
		macroStep(context);

	}

	protected FSMLogCallback createBasicLogCallback() {
		FSMLogCallback logCallback;
		logCallback = new FSMLogCallback() {
			private Logger logger = Logger.getLogger("com.nosolojava.fsm.BasicStateMachine");

			@Override
			public void logWarning(String text) {
				logger.log(Level.WARNING, text);
			}

			@Override
			public void logInfo(String text) {
				logger.log(Level.INFO, text);

			}

			@Override
			public void logError(String text) {
				logger.log(Level.SEVERE, text);

			}

			@Override
			public void logDebug(String text) {
				logger.log(Level.FINE, text);
			}

			@Override
			public void logDebug(String text, Object[] data) {
				logger.log(Level.FINE, text, data);
			}
		};
		return logCallback;
	}

	private void validateStates(Context context, State rootState) throws ConfigurationException {

		if (!rootState.isRootState()) {
			throw new ConfigurationException("First document states should be root state childrens.");
		}
		for (State state : rootState.getChildrens()) {
			validateStateRec(context, state);

		}
	}

	private void validateStateRec(Context context, State state) throws ConfigurationException {
		if (state.isRootState()) {
			throw new ConfigurationException("There can be only one root state");
		}
		// validate children for compound states
		if (state.hasChildrens()) {

			// validate transitions between parallel siblings
			if (state.isParallel()) {
				validateNoTransitionBetweenParallelSiblings(context, state);
			}
			for (State children : state.getChildrens()) {
				validateStateRec(context, children);
			}
		}

	}

	private void validateNoTransitionBetweenParallelSiblings(Context context, State parallelState)
			throws ParallelSiblingTransactionException {

		List<State> stateList = StateMachineUtils.stateToList(parallelState);
		for (State state : stateList) {
			State lca;
			for (Transition transition : state.getTransitions()) {
				if (transition.getTargetState(context) != null) {
					lca = calculateTransitionAncestor(context, transition);
					if (lca.equals(parallelState)) {
						throw new ParallelSiblingTransactionException(transition);
					}
				}
			}
		}

	}

	public void pushInternalEvent(Context context, Event event) {
		context.offerInternalEvent(event);
	}

	@Override
	public void handleExternalEvent(Event externalEvent, Context context) {

		if (DEBUG.get()) {
			logFine("####################################################################################");
			logFine("@@@ event {0}, receiving session id: {1}", new Object[] { externalEvent, context.getSessionId() });
		}

		// forth, manage if we have an external event
		if (externalEvent != null) {
			// register event in context
			context.setCurrentEvent(externalEvent);

			// manage invokes
			manageInvokes(context, externalEvent);

			// find transitions for the event
			List<Transition> enabledTransitions = selectNormalTransitions(context, externalEvent);

			// in there is any transition
			if (!enabledTransitions.isEmpty()) {
				if (DEBUG.get()) {
					logFine("--> transition/s FOUND " + enabledTransitions);
				}

				// micro step with normal transitions
				microstep(context, enabledTransitions);
				// manage internal events
				manageInternalEvents(context);

			}

		}

		// first ...
		macroStep(context);

	}

	protected void macroStep(Context context) {
		// first manage all internal events
		manageInternalEvents(context);

		// second call invokes
		callInvokes(context);

		// third manage all internal events that invoke could have raised
		manageInternalEvents(context);

		// notify  		
		for (FSMListener listener : listeners) {
			listener.onMacroStepFinished(context);
		}

		// check if final state is reached
		boolean finalState = isFinalStateReached(context);
		if (finalState) {
			exitInterpreter(context);
		}

		if (DEBUG.get()) {
			logFine("End macrostep, session id {0}, current event: {1}",
					new Object[] { context.getSessionId(), context.getCurrentEvent() });
			logFine("Active states {0}", new Object[] { context.getActiveStates() });

			logFine("####################################################################################");

		}
	}

	private boolean isFinalStateReached(Context context) {
		boolean finalState = false;
		Iterator<State> it = context.getActiveStates().iterator();
		while (!finalState && it.hasNext()) {
			State state = it.next();
			if (state.isFinal() && state.getParent().isRootState()) {
				finalState = true;
			}

		}
		return finalState;
	}

	private void manageInternalEvents(Context context) {
		List<Transition> enabledTransitions;
		boolean macroStepComplete = false;
		while (!macroStepComplete) {
			// search event less transitions
			enabledTransitions = selectEventLessTransitions(context);
			if (enabledTransitions.isEmpty()) {
				// search internal events - transitions
				if (!context.hasInternalEvents()) {
					macroStepComplete = true;
				} else {
					Event internalEvent = context.pollInternalEvent();

					if (DEBUG.get()) {
						logFine("@@@ internal event " + internalEvent);
					}

					context.setCurrentEvent(internalEvent);
					enabledTransitions = selectNormalTransitions(context, internalEvent);
				}
			}
			// if any eventless or internal event transitions are
			// founded
			if (!enabledTransitions.isEmpty()) {
				if (DEBUG.get()) {
					logFine("<--> internal transitions found: {0} ", new Object[] { enabledTransitions });
				}

				// micro step
				microstep(context, enabledTransitions);
			}
		}
	}

	private void callInvokes(Context context) {
		if (DEBUG.get()) {
			logFine("Check if there are invokes to call");
		}

		SortedSet<State> statesToInvoke = context.getStatesToInvoke();
		for (State state : statesToInvoke) {
			invokeAll(context, state);
		}
		context.clearStatesToInvoke();
	}

	private void manageInvokes(Context context, Event externalEvent) {
		for (State state : context.getActiveStates()) {
			String invokeId;
			for (Invoke invoke : state.getInvokes()) {
				invokeId = invoke.getId(context);
				if (invokeId.equals(externalEvent.getInvokeId())) {
					applyInvokeFinalize(context, invoke, externalEvent);
				}
				if (invoke.isAutoforward()) {
					if (DEBUG.get()) {
						logFine("forward event {0} to invoke session {1}", new Object[] { externalEvent.getName(),
								invoke.getId(context) });
					}
					invoke.manageEvent(externalEvent, context);
				}
			}
		}
	}

	private void exitInterpreter(Context context) {

		//if there is a parent session
		if (context.getParentSessionId() != null) {

			//create a done session event
			Message sessionDoneMessage = createDoneSessionMessage(context);

			// push message to all io processors
			for (IOProcessor ioProcessor : context.getIOProcessors()) {
				ioProcessor.sendMessage(sessionDoneMessage);
			}

		}

		//end this scxml session
		this.engine.endSession(context.getSessionId());
	}

	private Message createDoneSessionMessage(Context context) {
		String sessionDoneEventName = PlatformEvents.DONE_INVOKE_PREFIX.getEventName() + context.getSessionId();

		SortedSet<State> finalStates = context.getActiveStates();

		//there should be only one active state (a final state children of scxml)
		State finalState = finalStates.first();
		DoneData doneData = finalState.getDoneData();
		Serializable body = doneData != null ? doneData.evaluateDoneData(context) : null;

		Message message = BasicMessage.createSimpleSCXMLMessage(sessionDoneEventName, context.getParentSessionId(),
				body, context);

		return message;
	}

	private void microstep(Context context, List<Transition> enabledTransitions) {
		if (DEBUG.get()) {
			logFine("++++++++++++++++++++++++++++++++++++");
			logFine("Microstep, activeStates {0}", new Object[] { context.getActiveStates() });
			logFine("++++++++++++++++++++++++++++++++++++");
		}

		exitStates(context, enabledTransitions);
		executeTransitionContent(context, enabledTransitions);
		enterStates(context, enabledTransitions);

		if (DEBUG.get()) {
			logFine("END microstep, activeStates {0}", new Object[] { context.getActiveStates() });
			logFine("++++++++++++++++++++++++++++++++++++");
		}
	}

	private List<Transition> selectNormalTransitions(Context context, Event event) {
		return selectTransitions(context, event, false);
	}

	private List<Transition> selectEventLessTransitions(Context context) {
		return selectTransitions(context, null, true);

	}

	private List<Transition> selectTransitions(Context context, Event event, boolean eventless) {
		ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();

		Transition enabledTransition;
		// for each atomic state
		for (State atomicActiveState : context.getActiveStates()) {
			// if is not preempted
			if (!isPreempted(context, atomicActiveState, enabledTransitions)) {
				// check transitions for the atomic state or ancestors
				if (!eventless) {
					enabledTransition = searchNormalTransitionInAStateAndAncestors(context, atomicActiveState, event);
				} else {
					enabledTransition = searchEventlessTransitionInAStateAndAncestors(context, atomicActiveState);

				}
				if (enabledTransition != null) {
					enabledTransitions.add(enabledTransition);
				}

			}
		}

		return enabledTransitions;
	}

	private boolean isPreempted(Context context, State atomicActiveState, ArrayList<Transition> enabledTransitions) {
		boolean result = false;

		// for each enabled transition
		Iterator<Transition> transitionIter = enabledTransitions.iterator();
		Transition transition;
		while (!result && transitionIter.hasNext()) {
			transition = transitionIter.next();
			// find LCA for the transition
			// if transition has target state
			if (transition.getTargetState(context) != null) {

				State LCA = searchLCA(context, transition);
				// if LCA is the root --> preemted is true
				if (LCA.isRootState()) {
					result = true;
				} else {
					// search if LCA is an ancestor of the active state
					result = atomicActiveState.isDescendant(LCA);

				}
			} else {
				result = false;
			}
		}

		return result;
	}

	protected State searchLCA(Context context, Transition transition) {
		State[] states = new State[] { transition.getSourceState(context), transition.getTargetState(context) };
		return searchLCA(context, states);
	}

	protected State searchLCA(Context context, State[] states) {
		State LCA = context.getModel().getRootState();
		@SuppressWarnings("unchecked")
		Iterator<State>[] breadcrumbs = new Iterator[states.length];

		// get all the breadcrumbs
		boolean foundLCA = false;
		List<State> breadcrumb;
		for (int i = 0; i < states.length && !foundLCA; i++) {
			// if any state in the list is the root state
			if (states[i].isRootState()) {
				// LCA is root state --> LCA founded!!!
				foundLCA = true;
			} else {
				breadcrumb = states[i].getBreadCrumb();
				breadcrumbs[i] = breadcrumb.iterator();
			}
		}

		State candidate;
		State other;

		while (!foundLCA) {
			// take the first state from breadcrumbs
			candidate = breadcrumbs[0].hasNext() ? breadcrumbs[0].next() : null;
			if (candidate != null) {
				// and compare with all the other states
				for (int i = 1; i < states.length && !foundLCA; i++) {
					other = breadcrumbs[i].hasNext() ? breadcrumbs[i].next() : null;
					if (other == null || !other.equals(candidate)) {
						foundLCA = true;
					}
				}
				// if no other states is different from candidate
				if (!foundLCA) {
					// put candidate to LCA
					LCA = candidate;
				}

			} else {
				// there is no candidate so... LCA has been found
				foundLCA = true;
			}
		}

		return LCA;
	}

	private Transition searchEventlessTransitionInAStateAndAncestors(Context context, State activeState) {
		return searchTransitionInAStateAndAncestors(context, activeState, null, true);
	}

	private Transition searchNormalTransitionInAStateAndAncestors(Context context, State activeState, Event event) {
		return searchTransitionInAStateAndAncestors(context, activeState, event, false);
	}

	private Transition searchTransitionInAStateAndAncestors(Context context, State activeState, Event event,
			boolean eventLess) {
		Transition result = null;

		Transition transition = null;
		State auxState = activeState;

		// check in state and ancestors
		while (result == null && auxState != null) {
			Iterator<? extends Transition> iterTransition = activeState.getTransitions().iterator();
			// check any transition in document order
			while (result == null && iterTransition.hasNext()) {
				transition = iterTransition.next();
				if (eventLess) {
					result = checkEventlessTransition(context, transition);
				} else {
					result = checkNormalTransition(context, transition, event);
				}
			}
			auxState = auxState.getParent();
		}

		return result;
	}

	private Transition checkNormalTransition(Context context, Transition transition, Event event) {
		Transition result = null;
		// if the transition has event
		if (transition.getEventName() != null) {
			// and transition event starts like event name
			if (matchTransition(transition, event)) {
				// check guard condition
				result = checkGuardCondition(context, transition);
			}
		} else {
			// check guard condition
			result = checkGuardCondition(context, transition);

		}
		return result;
	}

	private Transition checkEventlessTransition(Context context, Transition transition) {
		Transition result = null;
		// if the transition has no event
		if (transition.getEventName() == null || "".equals(transition.getEventName())) {
			// check guard condition
			result = checkGuardCondition(context, transition);

		}
		return result;
	}

	private boolean matchTransition(Transition transition, Event event) {
		boolean result = false;

		String eventName = event.getName();

		String aux = transition.getEventName();
		aux = aux.endsWith(".") ? aux.substring(0, aux.lastIndexOf(".")) : aux;
		aux = aux.endsWith(".*") ? aux.substring(0, aux.lastIndexOf(".*")) : aux;
		String transitionEventName = aux;

		// if transition has more than one event
		if (transitionEventName.contains(" ")) {
			result = matchMultiEventTransition(eventName, transitionEventName);
		} else {
			// eventName = eventName.contains(".") ? eventName.split("\\.")[0] :
			// eventName;
			result = matchSingleEventTransition(eventName, transitionEventName);

		}

		return result;
	}

	private boolean matchSingleEventTransition(String eventName, String transitionEventName) {
		boolean result = false;

		// if the event name is equals to transition even name
		if (eventName.equals(transitionEventName)) {
			result = true;
		} else
		// if the transition event name is less or equals than the event name
		if (transitionEventName.length() <= eventName.length()) {
			// if (transitionEventName.contains(".")) {
			String[] eventTokens = eventName.split("\\.");

			// compare transition tokens with event tokens
			StringTokenizer stTransitionEventTokens = new StringTokenizer(transitionEventName, ".");
			String transitionEventToken;

			boolean match = true;
			for (int i = 0; i < eventTokens.length && match && stTransitionEventTokens.hasMoreTokens(); i++) {
				transitionEventToken = stTransitionEventTokens.nextToken();
				match = matchEventNameToken(eventTokens[i], transitionEventToken);
			}
			result = match && !stTransitionEventTokens.hasMoreTokens();
			// } else {
			// result = matchEventNameToken(eventName, transitionEventName);

			// }
		}
		return result;
	}

	private boolean matchMultiEventTransition(String eventName, String transitionEventName) {
		boolean result = false;

		StringTokenizer stTransitionEventNames = new StringTokenizer(transitionEventName, " ");
		String transitionEventToken;
		while (stTransitionEventNames.hasMoreElements() && !result) {
			transitionEventToken = stTransitionEventNames.nextToken();
			result = matchSingleEventTransition(eventName, transitionEventToken);
		}
		return result;
	}

	private boolean matchEventNameToken(String eventToken, String transitionEventToken) {
		boolean match;
		transitionEventToken = transitionEventToken.replaceAll("\\*", ".*");
		match = eventToken.matches(transitionEventToken);
		return match;
	}

	private Transition checkGuardCondition(Context context, Transition transition) {
		Transition result = null;
		if (transition.passGuardCondition(context)) {
			result = transition;
		}

		return result;
	}

	private void enterState(Context context, Transition enabledTransition) {
		ArrayList<Transition> aux = new ArrayList<Transition>(1);
		aux.add(enabledTransition);
		enterStates(context, aux);
	}

	private void enterStates(Context context, List<Transition> enabledTransitions) {
		// predefined entry order --> to fulfill determinism
		SortedSet<State> statesToEnter = new TreeSet<State>(StateMachineUtils.entryOrderComparator);
		Map<String, State> statesForDefaultEntry = new HashMap<String, State>();

		// add states to enter
		addStatesToEnter(context, enabledTransitions, statesToEnter, statesForDefaultEntry);

		if (DEBUG.get()) {
			logFine("Entering states " + statesToEnter);
		}

		// for each state to enter
		for (State state : statesToEnter) {
			if (DEBUG.get()) {
				logFine("--> on enter: " + state);
			}

			// add datamodel to context
			if(state.getDataModel()!=null){
				context.loadDataModel(state.getDataModel());
			}
			
			// add to states to invoke
			context.addStateToInvoke(state);

			// add to context
			context.addActiveState(state);

			// TODO check binding type

			// execute on entry content
			executeContent(context, state.getOnEntryExecutables());

			// and the initial transition (if it's necessary)
			if (statesForDefaultEntry.containsKey(state.getName())) {
				executeContent(context, state.getInitialState().getInitialTransition().getExecutables());
			}

			// manage if final state
			manageFinalInEnterState(context, state);

		}
		if (DEBUG.get()) {
			logFine("------------------------------------");

		}

	}

	private void executeTransitionContent(Context context, List<Transition> enabledTransitions) {
		if (DEBUG.get()) {
			logFine("Enabled transitions {0}", new Object[] { enabledTransitions });
		}
		for (Transition transition : enabledTransitions) {
			if (DEBUG.get()) {
				logFine("<--> {0}", new Object[] { transition.toStringVerbose() });
			}

			List<Executable> executables = transition.getExecutables();
			executeContent(context, executables);
		}
		if (DEBUG.get()) {
			logFine("------------------------------------");
		}
	}

	private void exitStates(Context context, List<Transition> enabledTransitions) {
		SortedSet<State> activeStates = context.getActiveStates();
		// get states to exit
		SortedSet<State> statesToExit = getStatesToExit(context, enabledTransitions);
		if (DEBUG.get()) {
			logFine("Exiting states: " + statesToExit);
		}

		// remove from states to invoke
		for (State state : statesToExit) {
			context.removeStateToInvoke(state);
		}

		// historic states
		manageHistoricStates(context, activeStates, statesToExit);

		for (State state : statesToExit) {
			if (DEBUG.get()) {
				logFine("<-- on exit: " + state);
			}

			List<Executable> onExitExecutables = state.getOnExitExecutables();

			executeContent(context, onExitExecutables);

			cancelInvokes(context, state);
			context.removeActiveState(state);
			
			//remove datamodel
			if(state.getDataModel()!=null){
				context.removeDatamodel(state.getDataModel());
			}

		}
		if (DEBUG.get()) {
			logFine("------------------------------------");
		}
	}

	private void manageHistoricStates(Context context, SortedSet<State> activeStates, SortedSet<State> statesToExit) {
		for (State state : statesToExit) {
			List<State> historyStates = state.getHistoryStates();

			for (State historyState : historyStates) {
				SortedSet<State> historyActiveStates;

				// retrieve the history active states
				if (HistoryTypes.SHALLOW.equals(historyState.getHistoryType())) {
					historyActiveStates = extractShallowHistory(activeStates, state);
				} else {
					historyActiveStates = extractDeepHistory(activeStates, state);
				}

				if (DEBUG.get()) {
					logFine("saving ({0}) states: {1}", new Object[] { historyState, historyActiveStates });
				}

				// save the historic states
				HashMap<String, SortedSet<State>> historyStatesMap = getHistoryMap(context);
				historyStatesMap.put(historyState.getName(), historyActiveStates);
			}
		}
	}

	private HashMap<String, SortedSet<State>> getHistoryMap(Context context) {
		HashMap<String, SortedSet<State>> historyStatesMap = context.getDataByName(HISTORY_STATES);
		return historyStatesMap;
	}

	private SortedSet<State> extractDeepHistory(SortedSet<State> activeStates, State state) {
		SortedSet<State> historyActiveStates = new TreeSet<State>();

		for (State active : activeStates) {
			if (active.isDescendant(state) && !active.hasChildrens()) {
				historyActiveStates.add(active);
			}
		}

		return historyActiveStates;
	}

	private SortedSet<State> extractShallowHistory(SortedSet<State> activeStates, State state) {
		SortedSet<State> historyActiveStates = new TreeSet<State>();
		for (State children : state.getChildrens()) {
			if (activeStates.contains(children)) {
				historyActiveStates.add(children);
				break;
			}
		}

		return historyActiveStates;
	}

	private void logFine(String text) {
		logCallback.logDebug(text);
	}

	private void logFine(String text, Object[] data) {
		logCallback.logDebug(text, data);
	}

	private SortedSet<State> getStatesToExit(Context context, List<Transition> enabledTransitions) {
		// predefined exit order --> to fulfill determinism
		SortedSet<State> statesToExit = new TreeSet<State>(StateMachineUtils.exitOrderComparator);

		for (Transition transition : enabledTransitions) {
			// if transition has target state
			if (transition.getTargetState(context) != null) {
				State ancestor = calculateTransitionAncestor(context, transition);
				SortedSet<State> activeStates = context.getActiveStates();

				for (State activeState : activeStates) {
					if (activeState.isDescendant(ancestor)) {
						statesToExit.add(activeState);
					}
				}
			}
		}
		return statesToExit;
	}

	private void addStatesToEnter(Context context, List<Transition> enabledTransitions, SortedSet<State> statesToEnter,
			Map<String, State> statesForDefaultEntry) {
		State targetState;
		// for each transition with target state
		for (Transition transition : enabledTransitions) {
			targetState = transition.getTargetState(context);
			if (targetState != null) {

				// calculate the ancestor
				State ancestor = calculateTransitionAncestor(context, transition);

				// add target state to states to enter with proper descendants
				addStatesToEnter(context, targetState, statesToEnter, statesForDefaultEntry);
				// add ancestors to states to enter
				addAncestorsToStatesToEnter(context, statesToEnter, statesForDefaultEntry, ancestor, targetState);

			}

		}
	}

	private void manageFinalInEnterState(Context context, State state) {
		if (state.isFinal()) {
			State parent = state.getParent();
			sendFinalEvent(context, state);

			State grandParent = parent.getParent();
			if (grandParent != null && grandParent.isParallel()) {
				boolean allFinal = true;
				Iterator<? extends State> childrenIter = state.getChildrens().iterator();
				State children;
				while (allFinal && childrenIter.hasNext()) {
					children = childrenIter.next();
					allFinal = isInFinalState(context, children);
				}
				if (allFinal) {
					sendFinalEvent(context, parent);
				}

			}
		}
	}

	private boolean isInFinalState(Context context, State state) {
		boolean result = false;

		if (!state.hasChildrens()) {
			result = state.isFinal();
		} else {
			if (!state.isParallel()) {
				Iterator<State> childrenIter = state.getChildrens().iterator();
				State children;
				while (!result & childrenIter.hasNext()) {
					children = childrenIter.next();
					result = context.isActiveState(children) && isInFinalState(context, children);
				}
			} else {
				Iterator<State> childrenIter = state.getChildrens().iterator();
				boolean allFinal = true;
				while (allFinal & childrenIter.hasNext()) {
					allFinal = isInFinalState(context, childrenIter.next());
				}
				result = allFinal;

			}
		}

		return result;
	}

	private void sendFinalEvent(Context context, State state) {
		Serializable body = state.getDoneData() != null ? state.getDoneData().evaluateDoneData(context) : null;
		Event event = new BasicEvent(
				StateMachineUtils.DONE_STATE_MF.format(new Object[] { state.getParent().getName() }), body);
		context.offerInternalEvent(event);
	}

	private void executeContent(Context context, List<Executable> onEntryExecutables) {

		for (Executable executable : onEntryExecutables) {
			executeContent(context, executable);
		}
	}

	private void executeContent(Context context, Executable executable) {
		if (DEBUG.get()) {
			logFine("exec: {0}", new Object[] { executable });
		}

		executable.run(context);
	}

	private void invokeAll(Context context, State state) {

		Collection<Invoke> invokes = state.getInvokes();
		for (Invoke invoke : invokes) {
			if (DEBUG.get()) {
				logFine("Found Invoke {0} in state {1}", new Object[] { invoke.getId(context), state.getName() });
			}
			invoke.call(state.getName(), context);
			if (DEBUG.get()) {
				logFine("Invoke called, resulting session id: {0}", new Object[] { invoke.getId(context) });
			}

		}
	}

	private void applyInvokeFinalize(Context context, Invoke invoke, Event event) {
		if (invoke.getFinalize() != null) {
			if (DEBUG.get()) {
				logFine("Apply invoke finalize, session id: {0}", new Object[] { invoke.getId(context) });
			}
			invoke.getFinalize().run(context);
		}
	}

	private void cancelInvokes(Context context, State state) {

		for (Invoke invoke : state.getInvokes()) {
			cancelInvoke(context, invoke);
		}

	}

	private void cancelInvoke(Context context, Invoke invoke) {
		if (DEBUG.get()) {
			logFine("Cancel invoke, session id: {0}", new Object[] { invoke.getId(context) });
		}

		invoke.cancel(context);
	}

	private void addAncestorsToStatesToEnter(Context context, SortedSet<State> statesToEnter,
			Map<String, State> statesForDefaultEntry, State ancestor, State targetState) {
		State aux = targetState.getParent();
		// for each proper ancestor (between parent and LCA)
		while (aux != null && !aux.equals(ancestor)) {
			// add state to ancestors
			statesToEnter.add(aux);
			if (aux.isParallel()) {
				for (State parallelChildren : aux.getChildrens()) {

					// check if exists an active descendant state
					boolean someStateToEnterIsDescendant = isAnyActiveDescendantState(statesToEnter, parallelChildren);
					if (!someStateToEnterIsDescendant) {
						addStatesToEnter(context, parallelChildren, statesToEnter, statesForDefaultEntry);
					}

				}

			}
			aux = aux.getParent();
		}
	}

	private boolean isAnyActiveDescendantState(SortedSet<State> statesToEnter, State parallelChildren) {
		boolean someStateToEnterIsDescendant = false;
		for (State stateToEnter : statesToEnter) {
			if (stateToEnter.isDescendant(parallelChildren)) {
				someStateToEnterIsDescendant = true;
			}

		}
		return someStateToEnterIsDescendant;
	}

	private void addStatesToEnter(Context context, State state, SortedSet<State> statesToEnter,
			Map<String, State> statesForDefaultEntry) {
		if (state.isHistoryState()) {
			HashMap<String, SortedSet<State>> historyStatesMap = getHistoryMap(context);

			// if there is some history saved
			if (historyStatesMap.containsKey(state.getName())) {
				SortedSet<State> historicStates = historyStatesMap.get(state.getName());
				for (State historicActiveState : historicStates) {
					addStatesToEnter(context, historicActiveState, statesToEnter, statesForDefaultEntry);

					addAncestorsToStatesToEnter(context, statesToEnter, statesForDefaultEntry, state.getParent(),
							historicActiveState);

				}
			} else {
				for (Transition historicTransition : state.getTransitions()) {
					State targetHistoricTransition = historicTransition.getTargetState(context);
					addStatesToEnter(context, targetHistoricTransition, statesToEnter, statesForDefaultEntry);
				}
			}

		} else {
			statesToEnter.add(state);

			// important order of if's
			if (state.isParallel()) {
				for (State children : state.getChildrens()) {
					addStatesToEnter(context, children, statesToEnter, statesForDefaultEntry);
				}
			} else if (state.hasChildrens()) {
				statesForDefaultEntry.put(state.getName(), state);
				State target = state.getInitialState().getInitialTransition().getTargetState(context);
				addStatesToEnter(context, target, statesToEnter, statesForDefaultEntry);
			}
		}

	}

	private State calculateTransitionAncestor(Context context, Transition transition) {
		State result = null;

		// if transition is internal and target state is descendant of source
		State targetState = transition.getTargetState(context);
		if (transition.isInternal() && targetState != null
				&& targetState.isDescendant(transition.getSourceState(context))) {
			// source is the LCA
			result = transition.getSourceState(context);
		} else {
			result = searchLCA(context, transition);
		}
		return result;
	}

	@Override
	public void registerListener(FSMListener listener) {
		this.listeners.add(listener);

	}

	@Override
	public void unRegisterListener(FSMListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	public void setEngine(StateMachineEngine engine) {
		this.engine = engine;
	}

	public void setLogCallback(FSMLogCallback logCallback) {
		this.logCallback = logCallback;
	}

	
}
