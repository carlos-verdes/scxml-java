package test.com.nosolojava.fsm.basic;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.nosolojava.fsm.impl.runtime.basic.BasicStateMachineEngine;
import com.nosolojava.fsm.model.config.exception.ConfigurationException;
import com.nosolojava.fsm.parser.exception.SCXMLParserException;
import com.nosolojava.fsm.runtime.StateMachineEngine;

public class ConsoleManualTesting {

	
	public static void main(String[] args) throws ConfigurationException, IOException, InterruptedException, SCXMLParserException {
		
		
		StateMachineEngine engine = new BasicStateMachineEngine();
		engine.start();
		
		engine.startFSMSession(URI.create("classpath:consoleSM.xml"));
		
		engine.shutdownAndWait(1, TimeUnit.MINUTES);
	}
}
