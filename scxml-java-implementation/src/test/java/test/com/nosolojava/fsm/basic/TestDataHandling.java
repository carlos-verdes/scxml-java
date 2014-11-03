package test.com.nosolojava.fsm.basic;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;

import com.nosolojava.fsm.impl.model.basic.BasicStateMachineModel;
import com.nosolojava.fsm.impl.model.basic.jexl.JexlFSMContext;
import com.nosolojava.fsm.impl.model.basic.state.BasicState;
import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineEngine;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineFramework;
import com.nosolojava.fsm.model.StateMachineModel;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.Context;

public class TestDataHandling {

	private static final BasicEvent NEXT_EVENT = new BasicEvent("next");

	@Test
	public void thatDatamodelScopeIsRespected() throws Exception{
		
		// set the debug on/off
		BasicStateMachineFramework.DEBUG.set(true);
		
		//low rate for checks on new sessions (be carefull in a mobile this could consume a lot of battery)
		BasicStateMachineEngine.CHECK_AVAILABLE_SESSIONS_PERIOD_IN_MILLIS.set(1);
		
		BasicStateMachineEngine engine = new BasicStateMachineEngine();
		engine.start();
		URI fsmModelUri = new URI("classpath:datamodelScopeSM.xml");
		Context ctx = engine.startFSMSession(fsmModelUri);
		
		//the asserts are in custom actions!!
		
		//just send some events to allow the sm to finish
		engine.pushEvent(ctx.getSessionId(), NEXT_EVENT);
		engine.pushEvent(ctx.getSessionId(), NEXT_EVENT);
		engine.pushEvent(ctx.getSessionId(), NEXT_EVENT);
		
		boolean endSuccess=engine.shutdownAndWait(100, TimeUnit.MILLISECONDS);

		Assert.assertTrue("FSM has not finished on time",endSuccess);
	}
	
	@Test
	public void thatFileContentIsLoadedFromURI() throws ConfigurationException,
			URISyntaxException, IOException, InterruptedException,
			SCXMLParserException {

		BasicState root = BasicState.createRootState();
		StateMachineModel model = new BasicStateMachineModel(root);

		Context ctx = new JexlFSMContext("sessionId", null, model, null, null);
		
		URL textFileUrl = ClassLoader.getSystemClassLoader().getResource("someJSONtest.txt");
		
		//the result would be an InputStream
		InputStream result = ctx.getDataFromURL(textFileUrl);
		Assert.assertNotNull(result);
	
	}

}
