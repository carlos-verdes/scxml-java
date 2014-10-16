package test.com.nosolojava.fsm.parser;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineEngine;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineFramework;
import com.nosolojava.fsm.impl.runtime.executable.externalcomm.basic.BasicMessage;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.model.state.HistoryTypes;
import com.nosolojava.fsm.model.state.InitialState;
import com.nosolojava.fsm.model.state.State;
import com.nosolojava.fsm.model.transition.Transition;
import com.nosolojava.fsm.parser.XppActionParser;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.StateMachineEngine;
import com.nosolojava.fsm.runtime.executable.externalcomm.Message;

public class TestXPPParser {

	private static final String CONNECTED_STATE = "connected-state";
	private static final String DISCONNECTED_STATE = "disconnected-state";
	private static final String MAIN_STATE = "main-state";
	private static final String EVENT1 = "event1";
	private static final String EVENT2 = "event2";
	private static final String X_PARAM = "x";
	private static final String Y_PARAM = "y";

	@Test(timeout=1000)
	public void parseSimpleXML() throws XmlPullParserException, ConfigurationException, IOException,
			InterruptedException, URISyntaxException, SCXMLParserException {

		// set DEBUG true|false
		BasicStateMachineFramework.DEBUG.set(true);
		// init engine
		StateMachineEngine engine = new BasicStateMachineEngine();
		engine.start();
		
		//start a state machine session
		Context ctx = engine.startFSMSession(new URI("classpath:simpleSM.xml"));
//		Context ctx = engine.startFSMSession(null, new URI("http://nosolojava.com/simpleSM.xml"));
//		Context ctx = engine.startFSMSession(null, new URI("file:///c:/Users/cverdes/workspace/eclipse-ws/scxml-java/scxml-java-implementation/src/test/resources/simpleSM.xml"));
		
		StateMachineModel smm = ctx.getModel();

		// check initial
		InitialState initial = smm.getRootState().getInitialState();
		Transition initialTransition = initial.getInitialTransition();
		Assert.assertNotNull(initialTransition);
		Assert.assertEquals(smm.getRootState(), initialTransition.getSourceState(ctx));
		Assert.assertEquals(MAIN_STATE, initialTransition.getTargetState(ctx).getName());

		State state = smm.getRootState().getChildrens().get(0);
		Assert.assertEquals(MAIN_STATE, state.getName());
		
		state=state.getInitialState().getInitialTransition().getTargetState(ctx);
		Assert.assertEquals(DISCONNECTED_STATE, state.getName());
		Transition transition = state.getTransitions().get(0);
		Assert.assertNotNull(transition);
		Assert.assertEquals(state, transition.getSourceState(ctx));
		Assert.assertEquals(CONNECTED_STATE, transition.getTargetState(ctx).getName());
		Assert.assertFalse(transition.isInternal());

		engine.shutdownAndWait(50, TimeUnit.MILLISECONDS);
	}

	@Test
	public void parseCompoundXML() throws XmlPullParserException, ConfigurationException, IOException,
			URISyntaxException, InterruptedException, SCXMLParserException {
		// init sm
		StateMachineEngine engine = new BasicStateMachineEngine();
		engine.start();
		Context ctx = engine.startFSMSession(null, new URI("classpath:compoundSM.xml"));

		StateMachineModel smm = ctx.getModel();

		// check initial
		InitialState initial = smm.getRootState().getInitialState();
		Assert.assertEquals("init1", initial.getId());
		Transition initialTransition = initial.getInitialTransition();
		Assert.assertNotNull(initialTransition);
		Assert.assertEquals(smm.getRootState(), initialTransition.getSourceState(ctx));
		Assert.assertEquals("parent", initialTransition.getTargetState(ctx).getName());

		State state = smm.getRootState().getChildrens().get(0);
		Assert.assertEquals("parent", state.getName());
		Assert.assertEquals("children", state.getInitialState().getInitialTransition().getTargetState(ctx).getName());
		Assert.assertTrue(state.hasChildrens());
		State childrenState = state.getChildrens().get(0);
		Assert.assertEquals("children", childrenState.getName());
		State parallel = smm.getRootState().getChildrens().get(2);
		Assert.assertTrue(parallel.isParallel());
		Assert.assertEquals("parallel", parallel.getName());
		List<State> parallelHistory = parallel.getHistoryStates();
		Assert.assertEquals(1, parallelHistory.size());
		Assert.assertEquals("h1", parallelHistory.get(0).getName());
		Assert.assertEquals(HistoryTypes.DEEP, parallelHistory.get(0).getHistoryType());
		Assert.assertEquals("parallelChild1", parallelHistory.get(0).getTransitions().get(0).getTargetState(ctx)
				.getName());

		Assert.assertEquals("parallelChild1", parallel.getChildrens().get(1).getName());
		Assert.assertEquals("parallelChild2", parallel.getChildrens().get(2).getName());

		engine.shutdownAndWait(500, TimeUnit.MILLISECONDS);

	}

	@Test(timeout = 50000)
	public void testExecutables() throws XmlPullParserException, IOException, ConfigurationException,
			URISyntaxException, InterruptedException, SCXMLParserException {
		//custom action parsers
		List<XppActionParser> actionParsers = new ArrayList<XppActionParser>();
		BarrierActionParser barrierAction = new BarrierActionParser();
		actionParsers.add(barrierAction);

		// init sm
		StateMachineEngine engine = new BasicStateMachineEngine(actionParsers);
		BasicStateMachineFramework.DEBUG.set(false);
		engine.start();

		Context ctx = engine.startFSMSession(null, new URI("classpath:executablesSM.xml"));

		// check init data
		Assert.assertEquals(new Integer(1), getX(ctx));
		Assert.assertEquals("enteringIf", getY(ctx));

		// send event1
		pushEvent(ctx, EVENT1);
		barrierAction.blockUntilAction("loop");
		Assert.assertEquals("Countries: 0-Spain 1-France 2-Italy", ctx.getDataByName("z"));
		Assert.assertEquals(new Integer(2), getX(ctx));
		barrierAction.blockUntilAction("elif");
		Assert.assertEquals("enteringElif", getY(ctx));

		// send event2
		pushEvent(ctx, EVENT2);
		barrierAction.blockUntilAction("loop");
		Assert.assertEquals(new Integer(3), getX(ctx));
		barrierAction.blockUntilAction("else");
		Assert.assertEquals("enteringElse", getY(ctx));

		// send event1
		pushEvent(ctx, EVENT1);

		// wait end
		barrierAction.blockUntilAction("end");
		engine.shutdownAndWait(500, TimeUnit.MILLISECONDS);
	}

	private void pushEvent(Context ctx, String eventName) {
		Message message = BasicMessage.createSimpleSCXMLMessage(eventName, ctx);
		ctx.getScxmlIOProcessor().sendMessage(message);
	}

	private String getY(Context ctx) {
		return ctx.getDataByName(Y_PARAM);
	}

	private Integer getX(Context ctx) {
		return ctx.getDataByName(X_PARAM);
	}

	public class AndroidEvent implements Serializable {
		private static final long serialVersionUID = 9179432023071249134L;
		private boolean status;

		public AndroidEvent(boolean status) {
			this.status = status;
		}

		public boolean isStatus() {
			return status;
		}

		public void setStatus(boolean status) {
			this.status = status;
		}

	}
}
