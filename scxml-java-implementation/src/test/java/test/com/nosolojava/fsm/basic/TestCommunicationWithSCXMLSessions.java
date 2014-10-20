package test.com.nosolojava.fsm.basic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Principal;

import org.junit.Assert;
import org.junit.Test;

import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.StateMachineEngine;

public class TestCommunicationWithSCXMLSessions extends AbstractTest {

	private static final String OFF_STATE = "off-state";

	@Override
	public String getFSMUri() {
		return "classpath:simpleSM.xml";
	}

	@Test
	public void sendEventReceiveMessageTest() throws ConfigurationException,
			URISyntaxException, IOException, InterruptedException,
			SCXMLParserException {
		// create a user credentials
		Principal userCredentials = new Principal() {
			@Override
			public String getName() {
				return "John";
			}
		};

		// create the end assert
		this.setAfterEndTask(new ContextTask() {

			@Override
			public void run(Context context, StateMachineEngine engine,boolean hasFinishedOnTime) {
				//state machine should be in off-state and has finished in time
				Assert.assertTrue("State machine hasn't finished in time", hasFinishedOnTime);
				Assert.assertTrue("State not expected",
						context.isActiveStateByName(OFF_STATE));
			}

		});

		// push a connect event
		engine.pushEvent(ctx.getSessionId(), new BasicEvent("connect",
				userCredentials));
		engine.pushEvent(ctx.getSessionId(), new BasicEvent("exit"));

	}

	// @Test
	// public void communicateSBetweenSessionsTest() throws
	// ConfigurationException, URISyntaxException, IOException,
	// InterruptedException, SCXMLParserException {
	// }

}
