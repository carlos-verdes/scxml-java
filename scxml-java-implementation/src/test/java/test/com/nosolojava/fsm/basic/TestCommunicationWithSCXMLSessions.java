package test.com.nosolojava.fsm.basic;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Principal;

import org.junit.Test;

import com.nosolojava.fsm.impl.runtime.basic.BasicEvent;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;

public class TestCommunicationWithSCXMLSessions extends AbstractTest{


	@Override
	public String getFSMUri() {
		return "classpath:simpleSM.xml";
	}

	
	@Test
	public void sendEventReceiveMessageTest() throws ConfigurationException, URISyntaxException, IOException, InterruptedException, SCXMLParserException {
		//create a user credentials
		Principal userCredentials= new Principal() {
			@Override
			public String getName() {
				return "John";
			}
		};
		//push a connect event
		engine.pushEvent(ctx.getSessionId(), new BasicEvent("connect", userCredentials));
		engine.pushEvent(ctx.getSessionId(), new BasicEvent("exit"));
		
		

	}


	
//	@Test
//	public void communicateSBetweenSessionsTest() throws ConfigurationException, URISyntaxException, IOException, InterruptedException, SCXMLParserException {
//	}

	
	
}
