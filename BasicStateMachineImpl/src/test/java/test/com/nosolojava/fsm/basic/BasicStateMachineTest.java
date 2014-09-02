package test.com.nosolojava.fsm.basic;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.nosolojava.fsm.impl.model.basic.BasicStateMachineModel;
import com.nosolojava.fsm.impl.model.basic.state.BasicState;
import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineEngine;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineFramework;
import com.nosolojava.fsm.impl.runtime.basic.StateMachineUtils;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.config.exception.ParallelSiblingTransactionException;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.Event;
import com.nosolojava.fsm.runtime.EventType;
import com.nosolojava.fsm.runtime.FSMListener;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.StateMachineFramework;
import com.nosolojava.fsm.runtime.executable.externalcomm.IOProcessor;

public class BasicStateMachineTest {

	@Test
	public void testFindLCA() throws ConfigurationException, URISyntaxException, IOException, InterruptedException {
		// create the next states a.b.c | a.b.d | e.f

		BasicState root = BasicState.createRootState();
		State a = BasicState.createBasicState("a", root);
		State b = BasicState.createBasicState("b", a);
		State c = BasicState.createBasicState("c", b);
		State d = BasicState.createBasicState("d", b);
		State e = BasicState.createBasicState("e", root);
		State f = BasicState.createBasicState("f", e);

		BasicStateMachineModel model = new BasicStateMachineModel(root);
		model.getRootState().setInitialStateName("a");

		BasicStateMachinePublicMethods sm = createSM();

		StateMachineEngine engine = new BasicStateMachineEngine();
		engine.start();
		URI fsmModelUri = new URI("classpath:LCAText.xml");
		Context context = engine.startFSMSession(fsmModelUri);

		Assert.assertEquals(b, sm.searchLCA(context, new State[] { c, d }));
		Assert.assertEquals(root, sm.searchLCA(context, new State[] { c, f }));

		engine.shutdownAndWait(50, TimeUnit.MILLISECONDS);
	}

	private BasicStateMachinePublicMethods createSM() throws ConfigurationException {
		BasicStateMachinePublicMethods sm = new BasicStateMachinePublicMethods();

		return sm;
	}

	// TODO test enter states

	// TODO test proper ancestor

	@Test
	// TODO move document order test to basic state test
	public void testDocumentOrder() throws ConfigurationException {
		BasicState root = BasicState.createRootState();
		State appState = BasicState.createParallelState("applicationStates", root);

		State viewState = BasicState.createBasicState("viewStates", appState);
		BasicState loginState = BasicState.createBasicState("loginStates", viewState);
		BasicState loginConnectedState = BasicState.createBasicState("login-connected", loginState);
		BasicState loginConnectingConnState = BasicState.createBasicState("login-connecting", loginState);
		BasicState loginDisConnState = BasicState.createBasicState("login-disconnected", loginState);
		BasicState rosterState = BasicState.createBasicState("rosterStates", viewState);
		BasicState rosterConnState = BasicState.createBasicState("roster-conn", rosterState);
		BasicState rosterDisconnState = BasicState.createBasicState("roster-disconn", rosterState);

		BasicState chatState = BasicState.createBasicState("chatStates", viewState);
		BasicState chatConnState = BasicState.createBasicState("chat-conn", chatState);
		BasicState chatDisconnState = BasicState.createBasicState("chat-disconn", chatState);

		SortedSet<State> statesInEntryOrder = StateMachineUtils.stateToOrderedSet(root);

		//MessageFormat mf = new MessageFormat("{0}({1})-->");
		//for (State state : statesInEntryOrder) {
		//System.out.print(mf.format(new Object[] { state.getName(), state.getDocumentOrder() }));
		//}
		//System.out.println();

		State[] expectedEntryOrder = new State[] { root, appState, viewState, loginState, loginConnectedState,
				loginConnectingConnState, loginDisConnState, rosterState, rosterConnState, rosterDisconnState,
				chatState, chatConnState, chatDisconnState };
		Assert.assertArrayEquals("States doesn't have expected document entry order.", expectedEntryOrder,
				statesInEntryOrder.toArray());

		SortedSet<State> statesInExitOrder = new TreeSet<State>(StateMachineUtils.exitOrderComparator);
		statesInExitOrder.addAll(StateMachineUtils.stateToList(root));

		State[] expectedExitOrder = new State[] { chatDisconnState, chatConnState, chatState, rosterDisconnState,
				rosterConnState, rosterState, loginDisConnState, loginConnectingConnState, loginConnectedState,
				loginState, viewState, appState, root };
		Assert.assertArrayEquals("States doesn't have expected exit order.", expectedExitOrder,
				statesInExitOrder.toArray());

	}

	@Test
	public void testParallelSiblingsInternalTransaction() throws ConfigurationException, IOException,
			URISyntaxException, InterruptedException {

		BasicStateMachineEngine engine = new BasicStateMachineEngine();
		engine.start();
		Context context = engine.startFSMSession(new URI("classpath:parallel.xml"));
		engine.pushEvent(context.getSessionId(), new BasicEvent("good"));

		engine.shutdownAndWait(50, TimeUnit.MILLISECONDS);
	}

	@Test(expected = ParallelSiblingTransactionException.class)
	public void testNoParallelSiblingsTransaction() throws ConfigurationException, IOException, URISyntaxException,
			InterruptedException {

		BasicStateMachineEngine engine = new BasicStateMachineEngine();
		engine.start();

		Context context = engine.startFSMSession(new URI("classpath:parallelErrorParallelTransition.xml"));

		engine.pushEvent(context.getSessionId(), new BasicEvent("bad"));
		engine.shutdownAndWait(50, TimeUnit.MILLISECONDS);

	}

	@Test
	/*
	 * 3.1.1 Basic State Machine Notation 3.12.1 Event Descriptors
	 */
	public void testSimpleTransitions() throws Exception {

		final BasicStateMachineEngine engine = new BasicStateMachineEngine();
		StateMachineFramework frameWork = engine.getStateMachineFramework();

		final MacroStepQueueListener listener = createFSMBlockingListener();
		frameWork.registerListener(listener);

		engine.start();

		Context context = engine.startFSMSession(URI.create("classpath:simpleTransitionsSM.xml"));

		Assert.assertArrayEquals("Not expected states", new String[] { "s", "s0" }, listener.getActiveStateNames());
		assertEventState(engine, context.getSessionId(), listener, "e", new String[] { "s", "s1" });
		assertEventState(engine, context.getSessionId(), listener, "e", new String[] { "s", "s2" });
		assertEventState(engine, context.getSessionId(), listener, "e.moreSpecific",
				new String[] { "s", "moreSpecific" });
		assertEventState(engine, context.getSessionId(), listener, "tree", new String[] { "s", "sMultiple" });
		assertEventState(engine, context.getSessionId(), listener, "error", new String[] { "s", "sError" });
		assertEventState(engine, context.getSessionId(), listener, "error.dataError", new String[] { "s", "sError" });
		assertEventState(engine, context.getSessionId(), listener, "error2.end", new String[] { "s", "sError2" });
		assertEventState(engine, context.getSessionId(), listener, "error3.finish", new String[] { "s", "sError3" });
		assertEventState(engine, context.getSessionId(), listener, "error4DataMapping", new String[] { "s", "sError4" });
		assertEventState(engine, context.getSessionId(), listener, "foo.bar", new String[] { "s", "sMultiple" });
		assertEventState(engine, context.getSessionId(), listener, "foo", new String[] { "s3" });

		engine.shutdownAndWait(50, TimeUnit.MILLISECONDS);

	}

	/**
	 * 3.1.2.2 Compound States and Transitions
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCompoundSMTransitions() throws Exception {

		final BasicStateMachineEngine engine = new BasicStateMachineEngine();
		StateMachineFramework frameWork = engine.getStateMachineFramework();

		final MacroStepQueueListener listener = createFSMBlockingListener();
		frameWork.registerListener(listener);

		engine.start();

		Context context = engine.startFSMSession(URI.create("classpath:compoundSM.xml"));
		listener.getActiveStateNames();

		// after init onentryStateS=false
		boolean onEntryStateS = context.getDataByName("onentryStateS");
		Assert.assertFalse(onEntryStateS);

		// event toS, onentryStateS=true
		engine.pushEvent(context.getSessionId(), new BasicEvent("toS"));
		listener.getActiveStateNames();
		onEntryStateS = context.getDataByName("onentryStateS");
		Assert.assertTrue(onEntryStateS);

		//send event internal should not enter on state S
		engine.pushEvent(context.getSessionId(), new BasicEvent("internal"));
		listener.getActiveStateNames();
		onEntryStateS = context.getDataByName("onentryStateS");
		Assert.assertFalse(onEntryStateS);

		//send event external should enter on state S
		engine.pushEvent(context.getSessionId(), new BasicEvent("external"));
		listener.getActiveStateNames();
		onEntryStateS = context.getDataByName("onentryStateS");
		Assert.assertTrue(onEntryStateS);

	}

	@Test
	public void testHistoryState() throws Exception {

		final BasicStateMachineEngine engine = new BasicStateMachineEngine();
		StateMachineFramework frameWork = engine.getStateMachineFramework();

		final MacroStepQueueListener listener = createFSMBlockingListener();
		frameWork.registerListener(listener);

		engine.start();

		Context context = engine.startFSMSession(URI.create("classpath:historySM.xml"));

		assertActiveStates(listener, new String[] { "s", "s1", "s11" });

		BasicStateMachineFramework.DEBUG.set(true);

		assertEventActiveStates(new BasicEvent("e"), context, engine, listener, new String[] { "s", "s1", "s13" });
		assertEventActiveStates(new BasicEvent("f"), context, engine, listener, new String[] { "s", "s1", "s13" });
		assertEventActiveStates(new BasicEvent("e"), context, engine, listener, new String[] { "s", "s1", "s12" });
		assertEventActiveStates(new BasicEvent("e"), context, engine, listener, new String[] { "t" });
		assertEventActiveStates(new BasicEvent("e"), context, engine, listener, new String[] { "s", "s1", "s12" });

	}

	@Test
	public void testScxmlInvokeHandler() throws Exception {
		final BasicStateMachineEngine engine = new BasicStateMachineEngine();
		StateMachineFramework frameWork = engine.getStateMachineFramework();

		final MacroStepQueueListener listener = createFSMBlockingListener();
		frameWork.registerListener(listener);

		engine.start();

		String parentSessionId = "session1";
		String childrenSessionId = "session2";
		Context context = engine.startFSMSession(parentSessionId, null, URI.create("classpath:invokeSM.xml"), null);
		assertActiveStates(listener, new String[] { "initState" });
		Assert.assertEquals(0, context.getDataByName("x"));
		Assert.assertEquals(0, context.getDataByName("y"));
		Assert.assertEquals(0, context.getDataByName("result"));

		//send event start --> invoke children session
		assertEventActiveStates(new BasicEvent("start"), context, engine, listener, new String[] { "invokingState" },
				parentSessionId);

		//check init of children session
		assertActiveStates(listener, new String[] { "calculatorInitState" }, childrenSessionId);
		Context childrenContext = engine.getSession(childrenSessionId);
		Assert.assertEquals(0, childrenContext.getDataByName("result"));

		//init x and y from parent
		assertEventActiveStates(new BasicEvent("enterX", 1), context, engine, listener,
				new String[] { "invokingState" }, parentSessionId);
		Assert.assertEquals(1, context.getDataByName("x"));

		assertEventActiveStates(new BasicEvent("enterY", 3), context, engine, listener,
				new String[] { "invokingState" }, parentSessionId);
		Assert.assertEquals(3, context.getDataByName("y"));

		//send event add
		assertEventActiveStates(new BasicEvent("sumXY"), context, engine, listener, new String[] { "invokingState" },
				parentSessionId);
		Assert.assertEquals(0, context.getDataByName("result"));
		assertActiveStates(listener, new String[] { "invokingState" }, parentSessionId);
		Assert.assertEquals(4, context.getDataByName("result"));

		assertActiveStates(listener, new String[] { "finalState" }, parentSessionId);
		Assert.assertEquals(3, context.getDataByName("lastNumber"));

		
		Assert.assertEquals("changedValue", context.getDataByExpression("invokeValideVar"));
		Assert.assertEquals("initialValue", context.getDataByExpression("invokeInvalidVar"));
	}

	@Test
	public void testDoneData() throws Exception {
		final BasicStateMachineEngine engine = new BasicStateMachineEngine();
		StateMachineFramework frameWork = engine.getStateMachineFramework();

		final MacroStepQueueListener listener = createFSMBlockingListener();
		frameWork.registerListener(listener);

		engine.start();

		Context context = engine.startFSMSession(URI.create("classpath:doneDataSM.xml"));
		assertActiveStates(listener, new String[] { "parallelState", "s", "s1", "t", "t1", "u", "u1" });
		Assert.assertEquals(0, context.getDataByExpression("x"));

		assertEventActiveStates(new BasicEvent("e"), context, engine, listener, new String[] { "parallelState", "s",
				"s2", "t", "t2", "u", "u2" }, context.getSessionId());
		Assert.assertEquals(4, context.getDataByExpression("x"));
	}

	protected void assertEventActiveStates(Event event, Context context, StateMachineEngine engine,
			final MacroStepQueueListener listener, String[] expectedActiveStates) {
		assertEventActiveStates(event, context, engine, listener, expectedActiveStates, null);
	}

	protected void assertEventActiveStates(Event event, Context context, StateMachineEngine engine,
			final MacroStepQueueListener listener, String[] expectedActiveStates, String sessionId) {

		IOProcessor scxmlIOProcessor = context.getScxmlIOProcessor();
		event = new BasicEvent(event.getName(), EventType.EXTERNAL, "", scxmlIOProcessor.getLocation(context
				.getSessionId()), scxmlIOProcessor.getName(), "", event.getData());
		engine.pushEvent(context.getSessionId(), event);

		if (sessionId == null) {
			assertActiveStates(listener, expectedActiveStates);
		} else {
			assertActiveStates(listener, expectedActiveStates, sessionId);
		}

	}

	protected void assertActiveStates(final MacroStepQueueListener listener, String[] expectedActiveStates) {
		String[] activeStates = listener.getActiveStateNames();
		Assert.assertArrayEquals(expectedActiveStates, activeStates);
	}

	protected void assertActiveStates(final MacroStepQueueListener listener, String[] expectedActiveStates,
			String sessionId) {
		String[] activeStates = listener.getActiveStateNames(sessionId);
		Assert.assertArrayEquals(expectedActiveStates, activeStates);
	}

	private void assertEventState(StateMachineEngine engine, String sessionId, MacroStepQueueListener listener,
			String event, String[] expectedStates) {
		engine.pushEvent(sessionId, new BasicEvent(event));
		String[] activeStates = listener.getActiveStateNames();
		Assert.assertArrayEquals("Not expected states", expectedStates, activeStates);

	}

	public static class MacroStepQueueListener implements FSMListener {

		Map<String, BlockingQueue<String[]>> mapActiveStatesQueue = new HashMap<String, BlockingQueue<String[]>>();

		//BlockingQueue<String[]> activeStatesQueue = new LinkedBlockingQueue<String[]>();

		public String[] getActiveStateNames() {
			String sessionId = mapActiveStatesQueue.entrySet().iterator().next().getKey();
			return getActiveStateNames(sessionId);
		}

		public String[] getActiveStateNames(Context context) {

			return getActiveStateNames(context.getSessionId());
		}

		public String[] getActiveStateNames(String sessionId) {

			String[] activeStateNames;
			try {
				BlockingQueue<String[]> activeStatesQueue = this.mapActiveStatesQueue.get(sessionId);
				activeStateNames = activeStatesQueue.take();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return activeStateNames;

		}

		@Override
		public void onMacroStepFinished(Context context) {

			SortedSet<State> activeStates = context.getActiveStates();
			String[] activeStateNames = extracActiveStateNames(activeStates);

			BlockingQueue<String[]> activeStatesQueue = getActiveStatesQueue(context);
			activeStatesQueue.offer(activeStateNames);

		}

		protected BlockingQueue<String[]> getActiveStatesQueue(Context context) {
			BlockingQueue<String[]> activeStatesQueue;
			if (this.mapActiveStatesQueue.containsKey(context.getSessionId())) {
				activeStatesQueue = this.mapActiveStatesQueue.get(context.getSessionId());
			} else {
				activeStatesQueue = new LinkedBlockingQueue<String[]>();
				this.mapActiveStatesQueue.put(context.getSessionId(), activeStatesQueue);
			}
			return activeStatesQueue;
		}

		protected String[] extracActiveStateNames(SortedSet<State> activeStates) {
			String[] activeStateNames = new String[activeStates.size()];

			Iterator<State> iter = activeStates.iterator();

			int i = 0;
			while (iter.hasNext()) {
				State state = (State) iter.next();
				activeStateNames[i] = state.getName();

				i++;
			}
			return activeStateNames;
		}

		protected void wait(final CyclicBarrier macroStepBarrier) {
			try {
				macroStepBarrier.await();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	}

	public static MacroStepQueueListener createFSMBlockingListener() {

		MacroStepQueueListener listener = new MacroStepQueueListener();
		return listener;
	}

	public class BasicStateMachinePublicMethods extends BasicStateMachineFramework {

		public BasicStateMachinePublicMethods() throws ConfigurationException {
			super();
		}

		@Override
		public State searchLCA(Context context, State[] states) {
			return super.searchLCA(context, states);
		}

	}

}
