package test.com.nosolojava.fsm.basic;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;

import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineEngine;
import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineFramework;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.Context;
import com.nosolojava.fsm.runtime.StateMachineEngine;

public abstract class AbstractTest {

	StateMachineEngine engine;
	Context ctx;


	public abstract String getFSMUri();
	
	@Before
	public void initSMContexts() throws ConfigurationException, URISyntaxException, IOException, SCXMLParserException{
		// set the debug on/off
		BasicStateMachineFramework.DEBUG.set(true);
		
		//low rate for checks on new sessions (be carefull in a mobile this could consume a lot of battery)
		BasicStateMachineEngine.CHECK_AVAILABLE_SESSIONS_PERIOD_IN_MILLIS.set(1);
		
		engine = new BasicStateMachineEngine();
		engine.start();
		URI fsmModelUri = new URI(getFSMUri());
		this.ctx = engine.startFSMSession(fsmModelUri);
		
	}
	
	@After
	public void shutdownEngine() throws InterruptedException{
		engine.shutdownAndWait(100, TimeUnit.MILLISECONDS);
		Logger.getLogger(this.getClass().getCanonicalName()).log(Level.INFO,"Finish test ");
	}
	

}
